package com.example.movie_service.controller;

import com.example.movie_service.exception.ResourceNotFoundException;
import com.example.movie_service.model.Movie;
import com.example.movie_service.service.MovieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/movies")
@Tag(name = "Movies", description = "Movie management APIs")
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping
    @Operation(summary = "Get all movies", description = "Returns a list of all available movies")
    public List<Movie> getAllMovies() {
        return movieService.getAllMovies();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get movie by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Movie found"),
            @ApiResponse(responseCode = "404", description = "Movie not found")
    })
    public ResponseEntity<Movie> getMovieById(
            @Parameter(description = "Movie ID") @PathVariable Long id) {
        return movieService.getMovieById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new movie")
    @ApiResponse(responseCode = "201", description = "Movie created successfully")
    public Movie createMovie(@Valid @RequestBody Movie movie) {
        return movieService.createMovie(movie);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing movie")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Movie updated"),
            @ApiResponse(responseCode = "404", description = "Movie not found")
    })
    public ResponseEntity<Movie> updateMovie(
            @PathVariable Long id,
            @Valid @RequestBody Movie movieDetails) {
        return ResponseEntity.ok(movieService.updateMovie(id, movieDetails));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a movie")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Movie deleted"),
            @ApiResponse(responseCode = "404", description = "Movie not found")
    })
    public void deleteMovie(@PathVariable Long id) {
        movieService.deleteMovie(id);
    }
}
