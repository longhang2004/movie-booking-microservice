package com.example.booking_service.adapter.outgoing.feign;

import com.example.booking_service.domain.model.ShowtimeSnapshot;
import com.example.booking_service.domain.port.outgoing.ShowtimePort;
import org.springframework.stereotype.Component;

@Component
public class ShowtimeAdapter implements ShowtimePort {

    private final ShowtimeClient showtimeClient;

    public ShowtimeAdapter(ShowtimeClient showtimeClient) {
        this.showtimeClient = showtimeClient;
    }

    @Override
    public ShowtimeSnapshot getById(Long showtimeId) {
        return showtimeClient.getShowtimeById(showtimeId).toSnapshot();
    }
}
