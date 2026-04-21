package com.example.payment_service.controller;

import com.example.payment_service.exception.ResourceNotFoundException;
import com.example.payment_service.model.Payment;
import com.example.payment_service.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
@Tag(name = "Payments", description = "Payment processing APIs")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Process a payment manually", description = "Payments are normally triggered automatically via Kafka when a booking is created")
    @ApiResponse(responseCode = "201", description = "Payment processed")
    public Payment processPayment(
            @Parameter(description = "Booking ID") @RequestParam Long bookingId,
            @Parameter(description = "User ID") @RequestParam Long userId,
            @Parameter(description = "Payment amount") @RequestParam double amount) {
        return paymentService.processPayment(bookingId, userId, amount);
    }

    @GetMapping("/{bookingId}")
    @Operation(summary = "Get payment status by booking ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment found"),
            @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    public ResponseEntity<Payment> getPaymentByBookingId(
            @Parameter(description = "Booking ID") @PathVariable Long bookingId) {
        Payment payment = paymentService.getPaymentByBookingId(bookingId);
        if (payment == null) {
            throw new ResourceNotFoundException("Payment not found for booking id: " + bookingId);
        }
        return ResponseEntity.ok(payment);
    }
}
