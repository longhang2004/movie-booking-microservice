package com.example.booking_service.adapter.incoming.kafka;

import com.example.booking_service.domain.port.incoming.BookingUseCase;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class PaymentCompletedListener {

    private static final Logger log = LoggerFactory.getLogger(PaymentCompletedListener.class);

    private final BookingUseCase bookingUseCase;
    private final ObjectMapper objectMapper;

    public PaymentCompletedListener(BookingUseCase bookingUseCase, ObjectMapper objectMapper) {
        this.bookingUseCase = bookingUseCase;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "payment-completed", groupId = "booking-service")
    public void onPaymentCompleted(String payload) {
        try {
            JsonNode node = objectMapper.readTree(payload);
            Long bookingId = node.get("bookingId").asLong();
            String status = node.get("status").asText();
            boolean success = "SUCCESS".equalsIgnoreCase(status);
            bookingUseCase.onPaymentCompleted(bookingId, success);
            log.info("Applied payment result {} to booking {}", status, bookingId);
        } catch (Exception ex) {
            log.error("Failed to process payment-completed event: {}", payload, ex);
            throw new IllegalStateException("Payment completed handling failed", ex);
        }
    }
}
