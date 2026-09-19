package com.example.showtime_service.service;

import com.example.showtime_service.client.MovieClient;
import com.example.showtime_service.client.TheaterClient;
import com.example.showtime_service.exception.ResourceNotFoundException;
import com.example.showtime_service.model.Showtime;
import com.example.showtime_service.repository.ShowtimeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    @Transactional(readOnly = true)
    public List<Showtime> getAllShowtimes() {
        return showtimeRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Showtime getShowtimeById(Long id) {
        return showtimeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Showtime not found with id: " + id));
    }

    @Transactional
    public Showtime createShowtime(Showtime showtime) {
        movieClient.getMovieById(showtime.getMovieId());
        theaterClient.getTheaterById(showtime.getTheaterId());
        showtime.setId(null);
        return showtimeRepository.save(showtime);
    }

    @Transactional
    public Showtime updateShowtime(Long id, Showtime showtimeDetails) {
        Showtime showtime = getShowtimeById(id);
        movieClient.getMovieById(showtimeDetails.getMovieId());
        theaterClient.getTheaterById(showtimeDetails.getTheaterId());
        showtime.setMovieId(showtimeDetails.getMovieId());
        showtime.setTheaterId(showtimeDetails.getTheaterId());
        showtime.setRoomId(showtimeDetails.getRoomId());
        showtime.setStartTime(showtimeDetails.getStartTime());
        showtime.setEndTime(showtimeDetails.getEndTime());
        showtime.setPrice(showtimeDetails.getPrice());
        return showtimeRepository.save(showtime);
    }

    @Transactional
    public void deleteShowtime(Long id) {
        Showtime showtime = getShowtimeById(id);
        showtimeRepository.delete(showtime);
    }
}
