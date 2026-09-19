package com.example.booking_service.domain.port.outgoing;

import java.time.Duration;
import java.util.List;

public interface SeatLockPort {

    boolean tryAcquire(Long showtimeId, List<String> seats, Duration ttl);

    void release(Long showtimeId, List<String> seats);
}
