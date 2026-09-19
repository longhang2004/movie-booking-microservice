package com.example.theater_service.repository;

import com.example.theater_service.model.Theater;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TheaterRepository extends JpaRepository<Theater, Long> {

    @EntityGraph(attributePaths = "rooms")
    @Override
    List<Theater> findAll();

    @EntityGraph(attributePaths = "rooms")
    @Override
    Optional<Theater> findById(Long id);
}