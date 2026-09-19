package com.example.showtime_service.seed;

import com.example.showtime_service.model.Showtime;
import com.example.showtime_service.repository.ShowtimeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Component
@Profile("!test")
public class ShowtimeSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ShowtimeSeeder.class);

    private final ShowtimeRepository showtimeRepository;

    public ShowtimeSeeder(ShowtimeRepository showtimeRepository) {
        this.showtimeRepository = showtimeRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (showtimeRepository.count() > 0) {
            return;
        }
        LocalDate day = LocalDate.now().plusDays(1);
        showtimeRepository.saveAll(List.of(
                showtime(1L, 1L, 1L, day, LocalTime.of(14, 0), 148, "150000"),
                showtime(2L, 1L, 2L, day, LocalTime.of(17, 30), 152, "180000"),
                showtime(3L, 2L, 4L, day, LocalTime.of(19, 0), 169, "200000"),
                showtime(6L, 1L, 1L, day.plusDays(1), LocalTime.of(20, 0), 166, "220000"),
                showtime(4L, 3L, 6L, day.plusDays(1), LocalTime.of(21, 0), 132, "160000")
        ));
        log.info("Seeded upcoming showtimes");
    }

    private Showtime showtime(Long movieId, Long theaterId, Long roomId, LocalDate day, LocalTime start, int durationMin, String price) {
        Showtime showtime = new Showtime();
        showtime.setMovieId(movieId);
        showtime.setTheaterId(theaterId);
        showtime.setRoomId(roomId);
        LocalDateTime startTime = LocalDateTime.of(day, start);
        showtime.setStartTime(startTime);
        showtime.setEndTime(startTime.plusMinutes(durationMin));
        showtime.setPrice(new BigDecimal(price));
        return showtime;
    }
}
