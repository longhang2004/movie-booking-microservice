package com.example.showtime_service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "showtimes")
@Getter
@Setter
@NoArgsConstructor
public class Showtime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "movie_id", nullable = false)
    private Long movieId;

    @NotNull
    @Column(name = "theater_id", nullable = false)
    private Long theaterId;

    @NotNull
    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @NotNull
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @NotNull
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @NotNull
    @DecimalMin("0.0")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;
}
