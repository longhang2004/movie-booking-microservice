package com.example.booking_service.adapter.outgoing.persistence;

import com.example.booking_service.domain.port.outgoing.OutboxPort;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class OutboxAdapter implements OutboxPort {

    private final OutboxJpaRepository outboxJpaRepository;

    public OutboxAdapter(OutboxJpaRepository outboxJpaRepository) {
        this.outboxJpaRepository = outboxJpaRepository;
    }

    @Override
    public void enqueue(String destination, String aggregateId, String payload) {
        OutboxEventEntity event = new OutboxEventEntity();
        event.setDestination(destination);
        event.setAggregateId(aggregateId);
        event.setPayload(payload);
        event.setStatus("PENDING");
        event.setCreatedAt(Instant.now());
        outboxJpaRepository.save(event);
    }
}
