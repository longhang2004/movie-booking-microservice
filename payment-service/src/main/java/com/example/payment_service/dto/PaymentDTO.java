package com.example.payment_service.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PaymentDTO {

    private Long id;
    private Long bookingId;
    private Long userId;
    private double amount;
    private String status;
    private String paymentMethod;
    private LocalDateTime paymentTime;

    // Constructors, getters, setters
}