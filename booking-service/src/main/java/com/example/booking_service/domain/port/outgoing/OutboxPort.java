package com.example.booking_service.domain.port.outgoing;

public interface OutboxPort {

    void enqueue(String destination, String aggregateId, String payload);
}
