package com.example.booking_service.adapter.outgoing.feign;

import com.example.booking_service.exception.ResourceNotFoundException;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "showtime-service", fallbackFactory = ShowtimeClientFallbackFactory.class)
public interface ShowtimeClient {

    @GetMapping("/showtimes/{id}")
    ShowtimeResponse getShowtimeById(@PathVariable Long id);
}

@Component
class ShowtimeClientFallbackFactory implements FallbackFactory<ShowtimeClient> {
    @Override
    public ShowtimeClient create(Throwable cause) {
        return id -> {
            throw new ResourceNotFoundException("Showtime service is unavailable for id: " + id);
        };
    }
}
