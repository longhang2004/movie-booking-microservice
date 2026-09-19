package com.example.booking_service.application;

import com.example.booking_service.domain.exception.SeatUnavailableException;
import com.example.booking_service.domain.model.Booking;
import com.example.booking_service.domain.model.BookingStatus;
import com.example.booking_service.domain.model.ShowtimeSnapshot;
import com.example.booking_service.domain.port.incoming.BookingUseCase;
import com.example.booking_service.domain.port.outgoing.BookingPersistencePort;
import com.example.booking_service.domain.port.outgoing.OutboxPort;
import com.example.booking_service.domain.port.outgoing.SeatLockPort;
import com.example.booking_service.domain.port.outgoing.ShowtimePort;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingApplicationServiceTest {

    @Mock
    private BookingPersistencePort bookingPersistencePort;
    @Mock
    private ShowtimePort showtimePort;
    @Mock
    private SeatLockPort seatLockPort;
    @Mock
    private OutboxPort outboxPort;

    private BookingApplicationService service;

    @BeforeEach
    void setUp() {
        service = new BookingApplicationService(
                bookingPersistencePort, showtimePort, seatLockPort, outboxPort, new ObjectMapper());
    }

    @Test
    void createBooking_locksSeatsPersistsAndEnqueuesOutbox() {
        when(showtimePort.getById(1L)).thenReturn(new ShowtimeSnapshot(1L, new BigDecimal("150000"), LocalDateTime.now()));
        when(seatLockPort.tryAcquire(eq(1L), anyList(), any(Duration.class))).thenReturn(true);
        when(bookingPersistencePort.save(any(Booking.class))).thenAnswer(inv -> {
            Booking booking = inv.getArgument(0);
            booking.assignId(42L);
            return booking;
        });

        Booking result = service.createBooking(new BookingUseCase.CreateBookingCommand(10L, 1L, List.of("a1", "A2"), "idem-1"));

        assertThat(result.getId()).isEqualTo(42L);
        assertThat(result.getStatus()).isEqualTo(BookingStatus.PENDING);
        assertThat(result.getTotalPrice()).isEqualByComparingTo("300000");
        assertThat(result.getSeats()).containsExactly("A1", "A2");
        verify(outboxPort).enqueue(eq("payment-requested"), eq("42"), anyString());
    }

    @Test
    void createBooking_whenSeatsLocked_throwsConflict() {
        when(showtimePort.getById(1L)).thenReturn(new ShowtimeSnapshot(1L, new BigDecimal("150000"), LocalDateTime.now()));
        when(seatLockPort.tryAcquire(eq(1L), anyList(), any(Duration.class))).thenReturn(false);

        assertThatThrownBy(() -> service.createBooking(
                new BookingUseCase.CreateBookingCommand(10L, 1L, List.of("A1"), "k")))
                .isInstanceOf(SeatUnavailableException.class);
        verify(bookingPersistencePort, never()).save(any());
    }

    @Test
    void createBooking_sameIdempotencyKey_returnsExisting() {
        Booking existing = Booking.create(10L, 1L, List.of("A1"), new BigDecimal("150000"), "idem-1");
        existing.assignId(7L);
        when(bookingPersistencePort.findByIdempotencyKey("idem-1")).thenReturn(Optional.of(existing));

        Booking result = service.createBooking(new BookingUseCase.CreateBookingCommand(10L, 1L, List.of("A1"), "idem-1"));

        assertThat(result.getId()).isEqualTo(7L);
        verify(seatLockPort, never()).tryAcquire(any(), anyList(), any());
    }

    @Test
    void onPaymentCompleted_success_confirmsBooking() {
        Booking booking = Booking.create(10L, 1L, List.of("A1"), new BigDecimal("150000"), "k");
        booking.assignId(5L);
        when(bookingPersistencePort.findById(5L)).thenReturn(Optional.of(booking));
        when(bookingPersistencePort.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        service.onPaymentCompleted(5L, true);

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        verify(seatLockPort, never()).release(any(), anyList());
    }

    @Test
    void onPaymentCompleted_failure_releasesSeats() {
        Booking booking = Booking.create(10L, 1L, List.of("A1"), new BigDecimal("150000"), "k");
        booking.assignId(5L);
        when(bookingPersistencePort.findById(5L)).thenReturn(Optional.of(booking));
        when(bookingPersistencePort.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        service.onPaymentCompleted(5L, false);

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.FAILED);
        verify(seatLockPort).release(1L, List.of("A1"));
        verify(bookingPersistencePort).deleteSeats(5L);
    }
}
