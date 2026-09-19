package com.example.booking_service.adapter.incoming.web;

import com.example.booking_service.domain.model.Booking;
import com.example.booking_service.domain.model.BookingStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record BookingResponse(
        Long id,
        Long userId,
        Long showtimeId,
        List<String> seats,
        BookingStatus status,
        BigDecimal totalPrice,
        Instant createdAt
) {
    public static BookingResponse from(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getUserId(),
                booking.getShowtimeId(),
                booking.getSeats(),
                booking.getStatus(),
                booking.getTotalPrice(),
                booking.getCreatedAt()
        );
    }
}
