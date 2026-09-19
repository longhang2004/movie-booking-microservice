package com.example.booking_service.adapter.outgoing.redis;

import com.example.booking_service.domain.port.outgoing.SeatLockPort;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Profile("test")
public class InMemorySeatLockAdapter implements SeatLockPort {

    private final Set<String> locks = ConcurrentHashMap.newKeySet();

    @Override
    public boolean tryAcquire(Long showtimeId, List<String> seats, Duration ttl) {
        List<String> keys = seats.stream().sorted().map(seat -> showtimeId + ":" + seat).toList();
        synchronized (locks) {
            for (String key : keys) {
                if (locks.contains(key)) {
                    return false;
                }
            }
            locks.addAll(keys);
            return true;
        }
    }

    @Override
    public void release(Long showtimeId, List<String> seats) {
        seats.forEach(seat -> locks.remove(showtimeId + ":" + seat));
    }
}
