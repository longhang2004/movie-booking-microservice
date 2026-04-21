package com.example.payment_service.service;

import com.example.payment_service.model.Payment;
import com.example.payment_service.repository.PaymentRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public PaymentService(PaymentRepository paymentRepository,
                          KafkaTemplate<String, String> kafkaTemplate) {
        this.paymentRepository = paymentRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    public Payment processPayment(Long bookingId, Long userId, double amount) {
        Payment payment = new Payment();
        payment.setBookingId(bookingId);
        payment.setUserId(userId);
        payment.setAmount(amount);
        payment.setStatus("PENDING");
        payment.setPaymentMethod("CREDIT_CARD");
        payment.setPaymentTime(LocalDateTime.now());

        boolean paymentSuccess = amount > 0;
        payment.setStatus(paymentSuccess ? "SUCCESS" : "FAILED");

        Payment savedPayment = paymentRepository.save(payment);

        String status = paymentSuccess ? "SUCCESS" : "FAILED";
        String message = String.format("{\"bookingId\": %d, \"status\": \"%s\"}", bookingId, status);
        kafkaTemplate.send("booking-update-topic", message);

        return savedPayment;
    }

    public Payment getPaymentByBookingId(Long bookingId) {
        return paymentRepository.findByBookingId(bookingId);
    }
}
