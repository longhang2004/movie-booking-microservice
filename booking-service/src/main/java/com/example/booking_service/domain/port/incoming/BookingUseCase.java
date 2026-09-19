package com.example.booking_service.domain.port.incoming;

import com.example.booking_service.domain.model.Booking;

import java.util.List;

public interface BookingUseCase {

    Booking createBooking(CreateBookingCommand command);

    List<Booking> listByUser(Long userId);

    Booking getById(Long bookingId);

    void cancel(Long bookingId, Long userId);

    void onPaymentCompleted(Long bookingId, boolean success);

    record CreateBookingCommand(Long userId, Long showtimeId, List<String> seats, String idempotencyKey) {
    }
}
