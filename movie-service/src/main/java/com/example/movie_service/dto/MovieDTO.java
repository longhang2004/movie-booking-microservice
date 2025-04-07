package com.example.movie_service.dto;

import lombok.Data;

@Data
public class MovieDTO {

    private Long id;
    private String title;
    private String genre;
    private int duration;
    private String releaseDate;
    private String description;
    private String director;

    // Constructor, getters, setters (sử dụng Lombok @Data)
}