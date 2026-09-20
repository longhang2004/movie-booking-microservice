package com.example.movie_service.service;

import com.example.platform.security.web.ResourceNotFoundException;
import com.example.movie_service.model.Movie;
import com.example.movie_service.repository.MovieRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MovieService {

    private final MovieRepository movieRepository;

    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    @Transactional(readOnly = true)
    public Page<Movie> searchMovies(String q, String genre, Pageable pageable) {
        String query = blankToNull(q);
        String genreFilter = blankToNull(genre);
        return movieRepository.search(query, genreFilter, pageable);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "movies", key = "#id")
    public Movie getMovieById(Long id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + id));
    }

    @Transactional
    @CacheEvict(value = {"movies", "movieSearch"}, allEntries = true)
    public Movie createMovie(Movie movie) {
        movie.setId(null);
        return movieRepository.save(movie);
    }

    @Transactional
    @CacheEvict(value = {"movies", "movieSearch"}, allEntries = true)
    public Movie updateMovie(Long id, Movie movieDetails) {
        Movie movie = getMovieById(id);
        movie.setTitle(movieDetails.getTitle());
        movie.setGenre(movieDetails.getGenre());
        movie.setDuration(movieDetails.getDuration());
        movie.setReleaseDate(movieDetails.getReleaseDate());
        movie.setDescription(movieDetails.getDescription());
        movie.setDirector(movieDetails.getDirector());
        return movieRepository.save(movie);
    }

    @Transactional
    @CacheEvict(value = {"movies", "movieSearch"}, allEntries = true)
    public void deleteMovie(Long id) {
        Movie movie = getMovieById(id);
        movieRepository.delete(movie);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
