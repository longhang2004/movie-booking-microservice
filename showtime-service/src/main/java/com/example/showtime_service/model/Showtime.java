package com.example.showtime_service.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class Showtime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long movieId;  // Liên kết với movie-service
    private Long theaterId; // Liên kết với theater-service
    private Long roomId;   // Liên kết với room trong theater-service
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private double price;

    // Constructors, getters, setters (sử dụng Lombok @Data)
}