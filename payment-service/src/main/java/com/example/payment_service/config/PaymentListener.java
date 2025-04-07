package com.example.payment_service.config;

import com.example.payment_service.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentListener {

    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;

    public PaymentListener(PaymentService paymentService, ObjectMapper objectMapper) {
        this.paymentService = paymentService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "payment-topic", groupId = "payment-group")
    public void listen(String message) {
        try {
            // Parse message từ JSON
            PaymentRequest paymentRequest = objectMapper.readValue(message, PaymentRequest.class);

            // Xử lý thanh toán
            paymentService.processPayment(paymentRequest.getBookingId(), paymentRequest.getUserId(), paymentRequest.getAmount());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Class nội bộ để map message từ Kafka
    private static class PaymentRequest {
        private Long bookingId;
        private Long userId;
        private double amount;

        // Getters, setters
        public Long getBookingId() { return bookingId; }
        public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public double getAmount() { return amount; }
        public void setAmount(double amount) { this.amount = amount; }
    }
}