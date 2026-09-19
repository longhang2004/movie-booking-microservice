package com.example.booking_service.adapter.incoming.web;

import com.example.booking_service.domain.port.incoming.BookingUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/bookings")
@Tag(name = "Bookings", description = "Hexagonal booking use-cases: Redis seat holds, outbox payment, saga compensation")
public class BookingController {

    private final BookingUseCase bookingUseCase;

    public BookingController(BookingUseCase bookingUseCase) {
        this.bookingUseCase = bookingUseCase;
    }

    @GetMapping("/me")
    @Operation(summary = "List bookings for the authenticated user")
    public List<BookingResponse> myBookings(@AuthenticationPrincipal Jwt jwt) {
        return bookingUseCase.listByUser(userId(jwt)).stream().map(BookingResponse::from).toList();
    }

    @GetMapping("/{bookingId}")
    @Operation(summary = "Get a booking by id")
    public BookingResponse getById(@PathVariable Long bookingId, @AuthenticationPrincipal Jwt jwt) {
        var booking = bookingUseCase.getById(bookingId);
        if (!booking.getUserId().equals(userId(jwt)) && !isAdmin(jwt)) {
            throw new org.springframework.security.access.AccessDeniedException("Forbidden");
        }
        return BookingResponse.from(booking);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a booking", description = "Holds seats in Redis, persists booking+outbox atomically, payment is processed asynchronously")
    public BookingResponse create(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody CreateBookingRequest request) {
        String key = idempotencyKey == null || idempotencyKey.isBlank() ? UUID.randomUUID().toString() : idempotencyKey;
        var command = new BookingUseCase.CreateBookingCommand(userId(jwt), request.showtimeId(), request.seats(), key);
        return BookingResponse.from(bookingUseCase.createBooking(command));
    }

    @DeleteMapping("/{bookingId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Cancel a booking and release seats")
    public void cancel(@PathVariable Long bookingId, @AuthenticationPrincipal Jwt jwt) {
        bookingUseCase.cancel(bookingId, userId(jwt));
    }

    private Long userId(Jwt jwt) {
        Number id = jwt.getClaim("userId");
        return id.longValue();
    }

    private boolean isAdmin(Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList("roles");
        return roles != null && roles.contains("ADMIN");
    }
}
