package com.example.showtime_service.client;

import com.example.showtime_service.dto.TheaterDTO;
import com.example.showtime_service.exception.DownstreamServiceException;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "theater-service", fallbackFactory = TheaterClientFallbackFactory.class)
public interface TheaterClient {

    @GetMapping("/theaters/{id}")
    TheaterDTO getTheaterById(@PathVariable Long id);
}

@Component
class TheaterClientFallbackFactory implements FallbackFactory<TheaterClient> {
    @Override
    public TheaterClient create(Throwable cause) {
        return id -> {
            throw new DownstreamServiceException("Theater service is unavailable", cause);
        };
    }
}
