package com.example.payment_service.service;

import com.example.payment_service.exception.ResourceNotFoundException;
import com.example.payment_service.gateway.ChargeCommand;
import com.example.payment_service.gateway.ChargeResult;
import com.example.payment_service.gateway.PaymentGateway;
import com.example.payment_service.model.Payment;
import com.example.payment_service.model.PaymentStatus;
import com.example.payment_service.repository.PaymentRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

@Service
public class PaymentService {

    public static final String PAYMENT_COMPLETED_TOPIC = "payment-completed";
    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final PaymentGateway paymentGateway;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public PaymentService(
            PaymentRepository paymentRepository,
            PaymentGateway paymentGateway,
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper) {
        this.paymentRepository = paymentRepository;
        this.paymentGateway = paymentGateway;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public Payment processPayment(Long bookingId, Long userId, BigDecimal amount) {
        return paymentRepository.findByBookingId(bookingId).orElseGet(() -> chargeNew(bookingId, userId, amount));
    }

    private Payment chargeNew(Long bookingId, Long userId, BigDecimal amount) {
        ChargeResult result = paymentGateway.charge(new ChargeCommand(bookingId, userId, amount));
        Payment payment = new Payment();
        payment.setBookingId(bookingId);
        payment.setUserId(userId);
        payment.setAmount(amount);
        payment.setPaymentMethod("SIMULATED_CARD");
        payment.setProviderReference(result.providerReference());
        payment.setPaymentTime(Instant.now());
        payment.setStatus(result.success() ? PaymentStatus.SUCCESS : PaymentStatus.FAILED);
        Payment saved = paymentRepository.save(payment);
        publishCompleted(saved);
        log.info("Processed payment {} for booking {} status={}", saved.getId(), bookingId, saved.getStatus());
        return saved;
    }

    @Transactional(readOnly = true)
    public Payment getPaymentByBookingId(Long bookingId) {
        return paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for booking id: " + bookingId));
    }

    private void publishCompleted(Payment payment) {
        try {
            String payload = objectMapper.writeValueAsString(Map.of(
                    "bookingId", payment.getBookingId(),
                    "status", payment.getStatus().name(),
                    "providerReference", payment.getProviderReference() == null ? "" : payment.getProviderReference()
            ));
            kafkaTemplate.send(PAYMENT_COMPLETED_TOPIC, String.valueOf(payment.getBookingId()), payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to publish payment completed event", e);
        }
    }
}
