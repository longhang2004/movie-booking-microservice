package com.example.movie_service.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Entity
@Data
@Schema(description = "Movie entity")
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title is required")
    @Schema(description = "Movie title", example = "Inception")
    private String title;

    @NotBlank(message = "Genre is required")
    @Schema(description = "Movie genre", example = "Sci-Fi")
    private String genre;

    @NotNull
    @Min(value = 1, message = "Duration must be at least 1 minute")
    @Schema(description = "Duration in minutes", example = "148")
    private int duration;

    @NotBlank(message = "Release date is required")
    @Schema(description = "Release date (YYYY-MM-DD)", example = "2010-07-16")
    private String releaseDate;

    @Schema(description = "Movie description")
    private String description;

    @NotBlank(message = "Director is required")
    @Schema(description = "Director name", example = "Christopher Nolan")
    private String director;
}