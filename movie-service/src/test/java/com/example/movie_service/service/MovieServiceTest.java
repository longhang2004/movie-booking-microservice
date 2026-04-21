package com.example.movie_service.service;

import com.example.movie_service.exception.ResourceNotFoundException;
import com.example.movie_service.model.Movie;
import com.example.movie_service.repository.MovieRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovieServiceTest {

    @Mock
    private MovieRepository movieRepository;

    @InjectMocks
    private MovieService movieService;

    private Movie movie;

    @BeforeEach
    void setUp() {
        movie = new Movie();
        movie.setId(1L);
        movie.setTitle("Inception");
        movie.setGenre("Sci-Fi");
        movie.setDuration(148);
        movie.setReleaseDate("2010-07-16");
        movie.setDescription("A mind-bending thriller");
        movie.setDirector("Christopher Nolan");
    }

    @Test
    void getAllMovies_returnsAllMovies() {
        when(movieRepository.findAll()).thenReturn(List.of(movie));

        List<Movie> result = movieService.getAllMovies();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Inception");
    }

    @Test
    void getMovieById_existingId_returnsMovie() {
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));

        Optional<Movie> result = movieService.getMovieById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("Inception");
    }

    @Test
    void getMovieById_nonExistingId_returnsEmpty() {
        when(movieRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Movie> result = movieService.getMovieById(99L);

        assertThat(result).isEmpty();
    }

    @Test
    void createMovie_savesAndReturnsMovie() {
        when(movieRepository.save(any(Movie.class))).thenReturn(movie);

        Movie result = movieService.createMovie(movie);

        assertThat(result.getTitle()).isEqualTo("Inception");
        verify(movieRepository, times(1)).save(movie);
    }

    @Test
    void updateMovie_existingId_updatesMovie() {
        Movie updatedDetails = new Movie();
        updatedDetails.setTitle("Interstellar");
        updatedDetails.setGenre("Sci-Fi");
        updatedDetails.setDuration(169);
        updatedDetails.setReleaseDate("2014-11-07");
        updatedDetails.setDescription("Space exploration");
        updatedDetails.setDirector("Christopher Nolan");

        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(movieRepository.save(any(Movie.class))).thenAnswer(inv -> inv.getArgument(0));

        Movie result = movieService.updateMovie(1L, updatedDetails);

        assertThat(result.getTitle()).isEqualTo("Interstellar");
        assertThat(result.getDuration()).isEqualTo(169);
    }

    @Test
    void updateMovie_nonExistingId_throwsResourceNotFoundException() {
        when(movieRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> movieService.updateMovie(99L, movie))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Movie not found with id: 99");
    }

    @Test
    void deleteMovie_existingId_deletesMovie() {
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));

        movieService.deleteMovie(1L);

        verify(movieRepository, times(1)).delete(movie);
    }

    @Test
    void deleteMovie_nonExistingId_throwsResourceNotFoundException() {
        when(movieRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> movieService.deleteMovie(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Movie not found with id: 99");
    }
}
