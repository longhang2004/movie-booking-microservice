package com.example.movie_service.repository;

import com.example.movie_service.model.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MovieRepository extends JpaRepository<Movie, Long> {

    @Query("""
            SELECT m FROM Movie m
            WHERE (:q IS NULL OR LOWER(m.title) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(m.director) LIKE LOWER(CONCAT('%', :q, '%')))
              AND (:genre IS NULL OR LOWER(m.genre) = LOWER(:genre))
            """)
    Page<Movie> search(@Param("q") String q, @Param("genre") String genre, Pageable pageable);
}
