package com.example.booking_service.application;

import com.example.booking_service.domain.exception.BookingConflictException;
import com.example.booking_service.domain.exception.SeatUnavailableException;
import com.example.booking_service.domain.model.Booking;
import com.example.booking_service.domain.port.incoming.BookingUseCase;
import com.example.booking_service.domain.port.outgoing.BookingPersistencePort;
import com.example.booking_service.domain.port.outgoing.OutboxPort;
import com.example.booking_service.domain.port.outgoing.SeatLockPort;
import com.example.booking_service.domain.port.outgoing.ShowtimePort;
import com.example.platform.security.web.ResourceNotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

@Service
public class BookingApplicationService implements BookingUseCase {

    public static final String PAYMENT_REQUESTED_TOPIC = "payment-requested";

    private final BookingPersistencePort bookingPersistencePort;
    private final ShowtimePort showtimePort;
    private final SeatLockPort seatLockPort;
    private final OutboxPort outboxPort;
    private final ObjectMapper objectMapper;

    public BookingApplicationService(
            BookingPersistencePort bookingPersistencePort,
            ShowtimePort showtimePort,
            SeatLockPort seatLockPort,
            OutboxPort outboxPort,
            ObjectMapper objectMapper) {
        this.bookingPersistencePort = bookingPersistencePort;
        this.showtimePort = showtimePort;
        this.seatLockPort = seatLockPort;
        this.outboxPort = outboxPort;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public Booking createBooking(CreateBookingCommand command) {
        List<String> seats = normalizeSeats(command.seats());
        if (command.idempotencyKey() != null && !command.idempotencyKey().isBlank()) {
            var existing = bookingPersistencePort.findByIdempotencyKey(command.idempotencyKey());
            if (existing.isPresent()) {
                return existing.get();
            }
        }

        var showtime = showtimePort.getById(command.showtimeId());
        if (!seatLockPort.tryAcquire(command.showtimeId(), seats, Duration.ofMinutes(10))) {
            throw new SeatUnavailableException("One or more seats are already held for this showtime");
        }

        BigDecimal total = showtime.price().multiply(BigDecimal.valueOf(seats.size()));
        Booking booking = Booking.create(command.userId(), command.showtimeId(), seats, total, command.idempotencyKey());
        try {
            Booking saved = bookingPersistencePort.save(booking);
            outboxPort.enqueue(PAYMENT_REQUESTED_TOPIC, String.valueOf(saved.getId()), toPayload(saved));
            return saved;
        } catch (DataIntegrityViolationException ex) {
            seatLockPort.release(command.showtimeId(), seats);
            throw new SeatUnavailableException("Seat already booked for this showtime");
        } catch (RuntimeException ex) {
            seatLockPort.release(command.showtimeId(), seats);
            throw ex;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Booking> listByUser(Long userId) {
        return bookingPersistencePort.findByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Booking getById(Long bookingId) {
        return bookingPersistencePort.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));
    }

    @Override
    @Transactional
    public void cancel(Long bookingId, Long userId) {
        Booking booking = getById(bookingId);
        if (!booking.getUserId().equals(userId)) {
            throw new BookingConflictException("Cannot cancel another user's booking");
        }
        booking.cancel();
        bookingPersistencePort.deleteSeats(booking.getId());
        bookingPersistencePort.save(booking);
        seatLockPort.release(booking.getShowtimeId(), booking.getSeats());
    }

    @Override
    @Transactional
    public void onPaymentCompleted(Long bookingId, boolean success) {
        Booking booking = getById(bookingId);
        if (success) {
            booking.confirm();
            bookingPersistencePort.save(booking);
            return;
        }
        booking.fail();
        bookingPersistencePort.deleteSeats(booking.getId());
        bookingPersistencePort.save(booking);
        seatLockPort.release(booking.getShowtimeId(), booking.getSeats());
    }

    private List<String> normalizeSeats(List<String> seats) {
        if (seats == null || seats.isEmpty()) {
            throw new IllegalArgumentException("At least one seat is required");
        }
        List<String> normalized = seats.stream()
                .map(String::trim)
                .map(String::toUpperCase)
                .filter(s -> !s.isBlank())
                .toList();
        if (new LinkedHashSet<>(normalized).size() != normalized.size()) {
            throw new IllegalArgumentException("Duplicate seats in request");
        }
        return normalized;
    }

    private String toPayload(Booking booking) {
        try {
            return objectMapper.writeValueAsString(Map.of(
                    "bookingId", booking.getId(),
                    "userId", booking.getUserId(),
                    "amount", booking.getTotalPrice(),
                    "seats", booking.getSeats()
            ));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to serialize payment outbox payload", e);
        }
    }
}
