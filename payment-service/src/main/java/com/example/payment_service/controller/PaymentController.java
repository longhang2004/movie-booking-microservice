package com.example.payment_service.controller;

import com.example.payment_service.model.Payment;
import com.example.payment_service.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payments")
@Tag(name = "Payments", description = "Idempotent payment status APIs. Charges are driven by Kafka payment-requested events.")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/{bookingId}")
    @Operation(summary = "Get payment status by booking ID")
    public Payment getPaymentByBookingId(@PathVariable Long bookingId, @AuthenticationPrincipal Jwt jwt) {
        Payment payment = paymentService.getPaymentByBookingId(bookingId);
        Number userId = jwt.getClaim("userId");
        boolean admin = jwt.getClaimAsStringList("roles") != null && jwt.getClaimAsStringList("roles").contains("ADMIN");
        if (!admin && !payment.getUserId().equals(userId.longValue())) {
            throw new org.springframework.security.access.AccessDeniedException("Forbidden");
        }
        return payment;
    }
}
