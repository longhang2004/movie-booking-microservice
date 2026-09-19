package com.example.booking_service.domain.port.outgoing;

import com.example.booking_service.domain.model.ShowtimeSnapshot;

public interface ShowtimePort {

    ShowtimeSnapshot getById(Long showtimeId);
}
