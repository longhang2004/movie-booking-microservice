package com.example.payment_service.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long bookingId; // Liên kết với booking-service
    private Long userId;
    private double amount;
    private String status; // e.g., "PENDING", "SUCCESS", "FAILED"
    private String paymentMethod; // e.g., "CREDIT_CARD", "PAYPAL"
    private LocalDateTime paymentTime;

    // Constructors, getters, setters (sử dụng Lombok @Data)
}