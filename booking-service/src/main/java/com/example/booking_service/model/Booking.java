package com.example.booking_service.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;  // ID của người dùng đặt vé
    private Long showtimeId; // Liên kết với showtime-service
    private LocalDateTime bookingTime;
    private String status; // e.g., "PENDING", "CONFIRMED", "CANCELLED"
    private double totalPrice;

    @ElementCollection
    private List<String> seats; // Danh sách ghế được đặt (nếu có)

    // Constructors, getters, setters (sử dụng Lombok @Data)
}