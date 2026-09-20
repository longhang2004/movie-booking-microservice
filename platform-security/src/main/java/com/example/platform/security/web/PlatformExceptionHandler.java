package com.example.platform.security.web;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Order
public class PlatformExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(PlatformExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return ProblemDetails.of(HttpStatus.NOT_FOUND, "Resource Not Found", "not-found",
                ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage, (a, b) -> a));
        ProblemDetail problem = ProblemDetails.of(HttpStatus.BAD_REQUEST, "Validation Error", "validation",
                "Validation failed", request.getRequestURI());
        problem.setProperty("errors", fieldErrors);
        return problem;
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return ProblemDetails.of(HttpStatus.FORBIDDEN, "Forbidden", "forbidden",
                "Access denied", request.getRequestURI());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail handleAuthentication(AuthenticationException ex, HttpServletRequest request) {
        return ProblemDetails.of(HttpStatus.UNAUTHORIZED, "Unauthorized", "unauthorized",
                "Authentication required", request.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex, HttpServletRequest request) {
        log.error("Unhandled error on {}", request.getRequestURI(), ex);
        return ProblemDetails.of(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "internal",
                "An unexpected error occurred", request.getRequestURI());
    }
}
