package com.example.booking_service.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class BookingDTO {

    private Long id;
    private Long userId;
    private Long showtimeId;
    private LocalDateTime bookingTime;
    private String status;
    private double totalPrice;
    private List<String> seats;

    // Constructors, getters, setters
}