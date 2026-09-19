package com.example.booking_service.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ShowtimeSnapshot(Long id, BigDecimal price, LocalDateTime startTime) {
}
