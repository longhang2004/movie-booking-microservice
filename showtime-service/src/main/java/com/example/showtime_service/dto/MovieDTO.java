package com.example.showtime_service.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class MovieDTO {
    private Long id;
    private String title;
    private String genre;
    private int duration;
    private LocalDate releaseDate;
    private String description;
    private String director;
}
