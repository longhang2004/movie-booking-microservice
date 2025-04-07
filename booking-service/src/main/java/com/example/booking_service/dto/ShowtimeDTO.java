package com.example.booking_service.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ShowtimeDTO {

    private Long id;
    private Long movieId;
    private Long theaterId;
    private Long roomId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private double price;

    // Constructors, getters, setters
}