package com.example.booking_service.application;

import com.example.booking_service.adapter.outgoing.persistence.OutboxEventEntity;
import com.example.booking_service.adapter.outgoing.persistence.OutboxJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Profile("!test")
public class OutboxRelayService {

    private static final Logger log = LoggerFactory.getLogger(OutboxRelayService.class);

    private final OutboxJpaRepository outboxJpaRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OutboxRelayService(OutboxJpaRepository outboxJpaRepository, KafkaTemplate<String, String> kafkaTemplate) {
        this.outboxJpaRepository = outboxJpaRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void publishPending() {
        List<OutboxEventEntity> events = outboxJpaRepository.findTop50ByStatusOrderByCreatedAtAsc("PENDING");
        for (OutboxEventEntity event : events) {
            kafkaTemplate.send(event.getDestination(), event.getAggregateId(), event.getPayload());
            event.setStatus("PUBLISHED");
            event.setPublishedAt(Instant.now());
            log.info("Published outbox event {} to {}", event.getId(), event.getDestination());
        }
    }
}
