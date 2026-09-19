package com.example.booking_service.adapter.outgoing.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookingJpaRepository extends JpaRepository<BookingJpaEntity, Long> {

    java.util.List<BookingJpaEntity> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<BookingJpaEntity> findByIdempotencyKey(String idempotencyKey);
}
