package com.example.booking_service.domain.port.outgoing;

import com.example.booking_service.domain.model.Booking;

import java.util.List;
import java.util.Optional;

public interface BookingPersistencePort {

    Booking save(Booking booking);

    Optional<Booking> findById(Long id);

    List<Booking> findByUserId(Long userId);

    Optional<Booking> findByIdempotencyKey(String idempotencyKey);

    void deleteSeats(Long bookingId);
}
