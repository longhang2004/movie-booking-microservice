package com.example.showtime_service.exception;

import com.example.platform.security.web.ProblemDetails;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DownstreamServiceException.class)
    public ProblemDetail handleDownstream(DownstreamServiceException ex, HttpServletRequest request) {
        return ProblemDetails.of(HttpStatus.SERVICE_UNAVAILABLE, "Downstream Unavailable", "downstream",
                ex.getMessage(), request.getRequestURI());
    }
}
