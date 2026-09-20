package com.example.platform.security.web;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import static org.assertj.core.api.Assertions.assertThat;

class ProblemDetailsTest {

    @Test
    void buildsRfc7807ProblemWithTimestampAndPath() {
        ProblemDetail problem = ProblemDetails.of(
                HttpStatus.NOT_FOUND, "Resource Not Found", "not-found", "Movie missing", "/movies/9");

        assertThat(problem.getStatus()).isEqualTo(404);
        assertThat(problem.getTitle()).isEqualTo("Resource Not Found");
        assertThat(problem.getDetail()).isEqualTo("Movie missing");
        assertThat(problem.getType().toString()).isEqualTo("https://api.moviebooking.com/errors/not-found");
        assertThat(problem.getProperties()).containsEntry("path", "/movies/9");
        assertThat(problem.getProperties()).containsKey("timestamp");
    }
}
