package com.example.movie_service.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "movies")
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Movie catalog entry")
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title is required")
    @Column(nullable = false)
    @Schema(description = "Movie title", example = "Inception")
    private String title;

    @NotBlank(message = "Genre is required")
    @Column(nullable = false)
    @Schema(description = "Movie genre", example = "Sci-Fi")
    private String genre;

    @NotNull
    @Min(value = 1, message = "Duration must be at least 1 minute")
    @Column(nullable = false)
    @Schema(description = "Duration in minutes", example = "148")
    private int duration;

    @NotNull(message = "Release date is required")
    @Column(name = "release_date", nullable = false)
    @Schema(description = "Release date", example = "2010-07-16")
    private LocalDate releaseDate;

    @Column(columnDefinition = "TEXT")
    @Schema(description = "Movie description")
    private String description;

    @NotBlank(message = "Director is required")
    @Column(nullable = false)
    @Schema(description = "Director name", example = "Christopher Nolan")
    private String director;
}
