package com.example.movie_service.controller;

import com.example.movie_service.model.Movie;
import com.example.movie_service.service.MovieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/movies")
@Tag(name = "Movies", description = "Movie catalog APIs with pagination, search and Redis caching")
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping
    @Operation(summary = "Search movies", description = "Paginated catalog search. Filter by title/director (q) and genre.")
    public Page<Movie> searchMovies(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String genre,
            @PageableDefault(size = 10) Pageable pageable) {
        return movieService.searchMovies(q, genre, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get movie by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Movie found"),
            @ApiResponse(responseCode = "404", description = "Movie not found")
    })
    public Movie getMovieById(@Parameter(description = "Movie ID") @PathVariable Long id) {
        return movieService.getMovieById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new movie (ADMIN)")
    public Movie createMovie(@Valid @RequestBody Movie movie) {
        return movieService.createMovie(movie);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing movie (ADMIN)")
    public Movie updateMovie(@PathVariable Long id, @Valid @RequestBody Movie movieDetails) {
        return movieService.updateMovie(id, movieDetails);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a movie (ADMIN)")
    public void deleteMovie(@PathVariable Long id) {
        movieService.deleteMovie(id);
    }
}
