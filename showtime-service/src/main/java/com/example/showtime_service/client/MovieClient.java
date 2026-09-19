package com.example.showtime_service.client;

import com.example.showtime_service.dto.MovieDTO;
import com.example.showtime_service.exception.DownstreamServiceException;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "movie-service", fallbackFactory = MovieClientFallbackFactory.class)
public interface MovieClient {

    @GetMapping("/movies/{id}")
    MovieDTO getMovieById(@PathVariable Long id);
}

@Component
class MovieClientFallbackFactory implements FallbackFactory<MovieClient> {
    @Override
    public MovieClient create(Throwable cause) {
        return id -> {
            throw new DownstreamServiceException("Movie service is unavailable", cause);
        };
    }
}
