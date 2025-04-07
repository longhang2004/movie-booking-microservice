package com.example.showtime_service.service;

import com.example.showtime_service.client.MovieClient;
import com.example.showtime_service.client.TheaterClient;
import com.example.showtime_service.model.Showtime;
import com.example.showtime_service.repository.ShowtimeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ShowtimeService {

    @Autowired
    private ShowtimeRepository showtimeRepository;

    @Autowired
    private MovieClient movieClient;

    @Autowired
    private TheaterClient theaterClient;

    public List<Showtime> getAllShowtimes() {
        return showtimeRepository.findAll();
    }

    public Optional<Showtime> getShowtimeById(Long id) {
        return showtimeRepository.findById(id);
    }

    public Showtime createShowtime(Showtime showtime) {
        // Kiểm tra xem movie và theater có tồn tại không (gọi Feign Client)
        movieClient.getMovieById(showtime.getMovieId());
        theaterClient.getTheaterById(showtime.getTheaterId());

        return showtimeRepository.save(showtime);
    }

    public Showtime updateShowtime(Long id, Showtime showtimeDetails) {
        Showtime showtime = showtimeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Showtime not found"));

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
                .orElseThrow(() -> new RuntimeException("Showtime not found"));
        showtimeRepository.delete(showtime);
    }
}