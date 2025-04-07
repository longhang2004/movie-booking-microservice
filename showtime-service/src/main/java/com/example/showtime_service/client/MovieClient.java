package com.example.showtime_service.client;

import com.example.showtime_service.dto.MovieDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// @FeignClient(name = "movie-service", url = "http://localhost:8091")
@FeignClient(name = "movie-service")
public interface MovieClient {

    @GetMapping("/{id}")
    MovieDTO getMovieById(@PathVariable Long id);
}