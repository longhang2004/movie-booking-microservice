package com.example.booking_service.adapter.outgoing.redis;

import com.example.booking_service.domain.port.outgoing.SeatLockPort;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@Profile("!test")
public class RedisSeatLockAdapter implements SeatLockPort {

    private final StringRedisTemplate redisTemplate;

    public RedisSeatLockAdapter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean tryAcquire(Long showtimeId, List<String> seats, Duration ttl) {
        List<String> keys = seats.stream().sorted().map(seat -> key(showtimeId, seat)).toList();
        String token = UUID.randomUUID().toString();
        List<String> acquired = new ArrayList<>();
        try {
            for (String redisKey : keys) {
                Boolean ok = redisTemplate.opsForValue().setIfAbsent(redisKey, token, ttl);
                if (!Boolean.TRUE.equals(ok)) {
                    releaseKeys(acquired);
                    return false;
                }
                acquired.add(redisKey);
            }
            return true;
        } catch (RuntimeException ex) {
            releaseKeys(acquired);
            throw ex;
        }
    }

    @Override
    public void release(Long showtimeId, List<String> seats) {
        List<String> keys = seats.stream().map(seat -> key(showtimeId, seat)).toList();
        releaseKeys(keys);
    }

    private void releaseKeys(List<String> keys) {
        if (!keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    private String key(Long showtimeId, String seat) {
        return "seat-lock:" + showtimeId + ":" + seat;
    }
}
