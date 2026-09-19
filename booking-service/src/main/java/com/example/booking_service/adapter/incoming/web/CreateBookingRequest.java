package com.example.booking_service.adapter.incoming.web;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateBookingRequest(
        @NotNull Long showtimeId,
        @NotEmpty List<String> seats
) {
}
