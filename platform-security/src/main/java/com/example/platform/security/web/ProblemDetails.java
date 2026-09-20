package com.example.platform.security.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import java.net.URI;
import java.time.Instant;

public final class ProblemDetails {

    public static final String TYPE_BASE = "https://api.moviebooking.com/errors/";

    private ProblemDetails() {
    }

    public static ProblemDetail of(HttpStatus status, String title, String typeSuffix, String detail, String path) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setType(URI.create(TYPE_BASE + typeSuffix));
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("path", path);
        return problem;
    }
}
