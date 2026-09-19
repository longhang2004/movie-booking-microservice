package com.example.movie_service.repository;

import com.example.movie_service.model.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MovieRepository extends JpaRepository<Movie, Long> {

    @Query(value = """
            SELECT * FROM movies m
            WHERE (:q IS NULL OR m.title ILIKE CONCAT('%', CAST(:q AS VARCHAR), '%')
                   OR m.director ILIKE CONCAT('%', CAST(:q AS VARCHAR), '%'))
              AND (:genre IS NULL OR m.genre ILIKE CAST(:genre AS VARCHAR))
            """,
            countQuery = """
            SELECT count(*) FROM movies m
            WHERE (:q IS NULL OR m.title ILIKE CONCAT('%', CAST(:q AS VARCHAR), '%')
                   OR m.director ILIKE CONCAT('%', CAST(:q AS VARCHAR), '%'))
              AND (:genre IS NULL OR m.genre ILIKE CAST(:genre AS VARCHAR))
            """,
            nativeQuery = true)
    Page<Movie> search(@Param("q") String q, @Param("genre") String genre, Pageable pageable);
}
