package com.example.booking_service.adapter.outgoing.persistence;

import com.example.booking_service.domain.model.Booking;
import com.example.booking_service.domain.port.outgoing.BookingPersistencePort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
public class BookingPersistenceAdapter implements BookingPersistencePort {

    private final BookingJpaRepository bookingJpaRepository;

    public BookingPersistenceAdapter(BookingJpaRepository bookingJpaRepository) {
        this.bookingJpaRepository = bookingJpaRepository;
    }

    @Override
    @Transactional
    public Booking save(Booking booking) {
        BookingJpaEntity entity = booking.getId() == null
                ? new BookingJpaEntity()
                : bookingJpaRepository.findById(booking.getId()).orElseGet(BookingJpaEntity::new);
        entity.setUserId(booking.getUserId());
        entity.setShowtimeId(booking.getShowtimeId());
        entity.setStatus(booking.getStatus());
        entity.setTotalPrice(booking.getTotalPrice());
        entity.setIdempotencyKey(booking.getIdempotencyKey());
        entity.setCreatedAt(booking.getCreatedAt());
        entity.setUpdatedAt(booking.getUpdatedAt());
        if (entity.getSeats().isEmpty() && booking.getStatus().name().equals("PENDING") && booking.getId() == null) {
            for (String seat : booking.getSeats()) {
                BookingSeatEntity seatEntity = new BookingSeatEntity();
                seatEntity.setBooking(entity);
                seatEntity.setShowtimeId(booking.getShowtimeId());
                seatEntity.setSeatNumber(seat);
                entity.getSeats().add(seatEntity);
            }
        }
        BookingJpaEntity saved = bookingJpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Booking> findById(Long id) {
        return bookingJpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Booking> findByUserId(Long userId) {
        return bookingJpaRepository.findByUserIdOrderByCreatedAtDesc(userId).stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<Booking> findByIdempotencyKey(String idempotencyKey) {
        return bookingJpaRepository.findByIdempotencyKey(idempotencyKey).map(this::toDomain);
    }

    @Override
    @Transactional
    public void deleteSeats(Long bookingId) {
        bookingJpaRepository.findById(bookingId).ifPresent(entity -> entity.getSeats().clear());
    }

    private Booking toDomain(BookingJpaEntity entity) {
        List<String> seats = entity.getSeats().stream().map(BookingSeatEntity::getSeatNumber).toList();
        return Booking.restore(
                entity.getId(),
                entity.getUserId(),
                entity.getShowtimeId(),
                seats,
                entity.getStatus(),
                entity.getTotalPrice(),
                entity.getIdempotencyKey(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getVersion()
        );
    }
}
