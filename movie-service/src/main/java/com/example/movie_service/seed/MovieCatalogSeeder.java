package com.example.movie_service.seed;

import com.example.movie_service.model.Movie;
import com.example.movie_service.repository.MovieRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@Profile("!test")
public class MovieCatalogSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(MovieCatalogSeeder.class);

    private final MovieRepository movieRepository;

    public MovieCatalogSeeder(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (movieRepository.count() > 0) {
            return;
        }
        movieRepository.saveAll(List.of(
                movie("Inception", "Sci-Fi", 148, LocalDate.of(2010, 7, 16),
                        "A thief who steals secrets through dream-sharing technology.", "Christopher Nolan"),
                movie("The Dark Knight", "Action", 152, LocalDate.of(2008, 7, 18),
                        "Batman faces the Joker in Gotham City.", "Christopher Nolan"),
                movie("Interstellar", "Sci-Fi", 169, LocalDate.of(2014, 11, 7),
                        "Explorers travel through a wormhole in space.", "Christopher Nolan"),
                movie("Parasite", "Thriller", 132, LocalDate.of(2019, 5, 30),
                        "A poor family infiltrates a wealthy household.", "Bong Joon-ho"),
                movie("Spirited Away", "Animation", 125, LocalDate.of(2001, 7, 20),
                        "A girl enters the spirit world to save her parents.", "Hayao Miyazaki"),
                movie("Dune: Part Two", "Sci-Fi", 166, LocalDate.of(2024, 3, 1),
                        "Paul Atreides unites with the Fremen on Arrakis.", "Denis Villeneuve"),
                movie("Everything Everywhere All at Once", "Comedy", 139, LocalDate.of(2022, 3, 25),
                        "An exhausted immigrant is swept into a multiverse battle.", "Daniels"),
                movie("Whiplash", "Drama", 107, LocalDate.of(2014, 10, 10),
                        "A young drummer pushed to the edge by his instructor.", "Damien Chazelle")
        ));
        log.info("Seeded movie catalog");
    }

    private Movie movie(String title, String genre, int duration, LocalDate releaseDate, String description, String director) {
        Movie movie = new Movie();
        movie.setTitle(title);
        movie.setGenre(genre);
        movie.setDuration(duration);
        movie.setReleaseDate(releaseDate);
        movie.setDescription(description);
        movie.setDirector(director);
        return movie;
    }
}
