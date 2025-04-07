package com.example.booking_service.client;

import com.example.booking_service.dto.ShowtimeDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// @FeignClient(name = "showtime-service", url = "http://localhost:8093")
@FeignClient(name = "showtime-service")
public interface ShowtimeClient {

    @GetMapping("/{id}")
    ShowtimeDTO getShowtimeById(@PathVariable Long id);
}