package com.example.booking_service.controller;

import com.example.booking_service.model.Booking;
import com.example.booking_service.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bookings")
@Tag(name = "Bookings", description = "Ticket booking and seat management APIs")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get all bookings for a user")
    public List<Booking> getBookingsByUserId(
            @Parameter(description = "User ID") @PathVariable Long userId) {
        return bookingService.getBookingsByUserId(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new booking", description = "Books seats for a showtime and triggers async payment processing via Kafka")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Booking created, payment initiated"),
            @ApiResponse(responseCode = "404", description = "Showtime not found")
    })
    public Booking createBooking(
            @Parameter(description = "User ID") @RequestParam Long userId,
            @Parameter(description = "Showtime ID") @RequestParam Long showtimeId,
            @RequestBody List<String> seats) {
        return bookingService.createBooking(userId, showtimeId, seats);
    }

    @DeleteMapping("/{bookingId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Cancel a booking")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Booking cancelled"),
            @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    public void cancelBooking(
            @Parameter(description = "Booking ID") @PathVariable Long bookingId) {
        bookingService.cancelBooking(bookingId);
    }
}
