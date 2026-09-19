package com.example.booking_service.adapter.outgoing.feign;

import com.example.booking_service.domain.model.ShowtimeSnapshot;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ShowtimeResponse {
    private Long id;
    private Long movieId;
    private Long theaterId;
    private Long roomId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal price;

    public ShowtimeSnapshot toSnapshot() {
        return new ShowtimeSnapshot(id, price, startTime);
    }
}
