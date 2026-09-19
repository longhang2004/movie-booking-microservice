package com.example.booking_service.adapter.outgoing.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboxJpaRepository extends JpaRepository<OutboxEventEntity, Long> {

    List<OutboxEventEntity> findTop50ByStatusOrderByCreatedAtAsc(String status);
}
