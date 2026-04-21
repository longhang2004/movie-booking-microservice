package com.example.showtime_service.client;

import com.example.showtime_service.dto.TheaterDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "theater-service")
public interface TheaterClient {

    @GetMapping("/theaters/{id}")
    TheaterDTO getTheaterById(@PathVariable Long id);
}
