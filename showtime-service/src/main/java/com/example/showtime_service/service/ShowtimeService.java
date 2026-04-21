package com.example.showtime_service.service;

import com.example.showtime_service.client.MovieClient;
import com.example.showtime_service.client.TheaterClient;
import com.example.showtime_service.exception.ResourceNotFoundException;
import com.example.showtime_service.model.Showtime;
import com.example.showtime_service.repository.ShowtimeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ShowtimeService {

    private final ShowtimeRepository showtimeRepository;
    private final MovieClient movieClient;
    private final TheaterClient theaterClient;

    public ShowtimeService(ShowtimeRepository showtimeRepository,
                           MovieClient movieClient,
                           TheaterClient theaterClient) {
        this.showtimeRepository = showtimeRepository;
        this.movieClient = movieClient;
        this.theaterClient = theaterClient;
    }

    public List<Showtime> getAllShowtimes() {
        return showtimeRepository.findAll();
    }

    public Optional<Showtime> getShowtimeById(Long id) {
        return showtimeRepository.findById(id);
    }

    public Showtime createShowtime(Showtime showtime) {
        movieClient.getMovieById(showtime.getMovieId());
        theaterClient.getTheaterById(showtime.getTheaterId());
        return showtimeRepository.save(showtime);
    }

    public Showtime updateShowtime(Long id, Showtime showtimeDetails) {
        Showtime showtime = showtimeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Showtime not found with id: " + id));

        showtime.setMovieId(showtimeDetails.getMovieId());
        showtime.setTheaterId(showtimeDetails.getTheaterId());
        showtime.setRoomId(showtimeDetails.getRoomId());
        showtime.setStartTime(showtimeDetails.getStartTime());
        showtime.setEndTime(showtimeDetails.getEndTime());
        showtime.setPrice(showtimeDetails.getPrice());

        return showtimeRepository.save(showtime);
    }

    public void deleteShowtime(Long id) {
        Showtime showtime = showtimeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Showtime not found with id: " + id));
        showtimeRepository.delete(showtime);
    }
}
