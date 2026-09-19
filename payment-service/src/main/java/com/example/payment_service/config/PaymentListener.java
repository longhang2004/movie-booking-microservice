package com.example.payment_service.config;

import com.example.payment_service.service.PaymentService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Profile("!test")
public class PaymentListener {

    private static final Logger log = LoggerFactory.getLogger(PaymentListener.class);

    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;

    public PaymentListener(PaymentService paymentService, ObjectMapper objectMapper) {
        this.paymentService = paymentService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "payment-requested", groupId = "payment-service")
    public void onPaymentRequested(String message) {
        try {
            JsonNode node = objectMapper.readTree(message);
            Long bookingId = node.get("bookingId").asLong();
            Long userId = node.get("userId").asLong();
            BigDecimal amount = node.get("amount").decimalValue();
            paymentService.processPayment(bookingId, userId, amount);
        } catch (Exception ex) {
            log.error("Failed to consume payment-requested event: {}", message, ex);
            throw new IllegalStateException("Payment requested handling failed", ex);
        }
    }
}
