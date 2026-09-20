package com.example.booking_service.exception;

import com.example.booking_service.domain.exception.BookingConflictException;
import com.example.booking_service.domain.exception.SeatUnavailableException;
import com.example.platform.security.web.ProblemDetails;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({SeatUnavailableException.class, BookingConflictException.class})
    public ProblemDetail handleConflict(RuntimeException ex, HttpServletRequest request) {
        return ProblemDetails.of(HttpStatus.CONFLICT, "Conflict", "conflict",
                ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        return ProblemDetails.of(HttpStatus.BAD_REQUEST, "Bad Request", "bad-request",
                ex.getMessage(), request.getRequestURI());
    }
}
