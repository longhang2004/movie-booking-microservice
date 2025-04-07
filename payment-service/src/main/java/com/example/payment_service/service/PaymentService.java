package com.example.payment_service.service;

import com.example.payment_service.model.Payment;
import com.example.payment_service.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public Payment processPayment(Long bookingId, Long userId, double amount) {
        Payment payment = new Payment();
        payment.setBookingId(bookingId);
        payment.setUserId(userId);
        payment.setAmount(amount);
        payment.setStatus("PENDING");
        payment.setPaymentMethod("CREDIT_CARD");
        payment.setPaymentTime(LocalDateTime.now());

        // Giả lập xử lý thanh toán
        boolean paymentSuccess = simulatePaymentProcessing(amount);
        payment.setStatus(paymentSuccess ? "SUCCESS" : "FAILED");

        Payment savedPayment = paymentRepository.save(payment);

        // Gửi thông báo đến booking-service qua Kafka
        if (paymentSuccess) {
            String message = String.format("{\"bookingId\": %d, \"status\": \"SUCCESS\"}", bookingId);
            kafkaTemplate.send("booking-update-topic", message);
        } else {
            String message = String.format("{\"bookingId\": %d, \"status\": \"FAILED\"}", bookingId);
            kafkaTemplate.send("booking-update-topic", message);
        }

        return savedPayment;
    }

    private boolean simulatePaymentProcessing(double amount) {
        return amount >0 ; // Giả lập
    }

    public Payment getPaymentByBookingId(Long bookingId) {
        return paymentRepository.findByBookingId(bookingId);
    }
}