package com.example.payment_service.service;

import com.example.payment_service.gateway.ChargeResult;
import com.example.payment_service.gateway.PaymentGateway;
import com.example.payment_service.model.Payment;
import com.example.payment_service.model.PaymentStatus;
import com.example.payment_service.repository.PaymentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private PaymentGateway paymentGateway;
    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(paymentRepository, paymentGateway, kafkaTemplate, new ObjectMapper());
    }

    @Test
    void processPayment_newBooking_chargesAndPublishes() {
        when(paymentRepository.findByBookingId(9L)).thenReturn(Optional.empty());
        when(paymentGateway.charge(any())).thenReturn(ChargeResult.ok("sim-1"));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> {
            Payment payment = inv.getArgument(0);
            payment.setId(1L);
            return payment;
        });

        Payment result = paymentService.processPayment(9L, 3L, new BigDecimal("150000"));

        assertThat(result.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        verify(kafkaTemplate).send(anyString(), anyString(), anyString());
    }

    @Test
    void processPayment_existingBooking_isIdempotent() {
        Payment existing = new Payment();
        existing.setId(1L);
        existing.setBookingId(9L);
        existing.setStatus(PaymentStatus.SUCCESS);
        when(paymentRepository.findByBookingId(9L)).thenReturn(Optional.of(existing));

        Payment result = paymentService.processPayment(9L, 3L, new BigDecimal("150000"));

        assertThat(result.getId()).isEqualTo(1L);
        verify(paymentGateway, never()).charge(any());
        verify(kafkaTemplate, never()).send(anyString(), anyString(), anyString());
    }
}
