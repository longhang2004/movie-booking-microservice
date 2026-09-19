package com.example.theater_service.seed;

import com.example.theater_service.model.Room;
import com.example.theater_service.model.Theater;
import com.example.theater_service.repository.TheaterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("!test")
public class TheaterSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(TheaterSeeder.class);

    private final TheaterRepository theaterRepository;

    public TheaterSeeder(TheaterRepository theaterRepository) {
        this.theaterRepository = theaterRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (theaterRepository.count() > 0) {
            return;
        }
        theaterRepository.saveAll(List.of(
                theater("CGV Vincom Dong Khoi", "72 Le Thanh Ton, District 1, HCMC", "028-3827-8698",
                        room("IMAX", 280), room("Hall 2", 160), room("Hall 3", 120)),
                theater("Lotte Cinema Landmark 81", "720A Dien Bien Phu, Binh Thanh, HCMC", "028-7300-5588",
                        room("Screen X", 220), room("Hall 1", 150)),
                theater("BHD Star Bitexco", "2 Hai Trieu, District 1, HCMC", "028-6263-3636",
                        room("Premium", 90), room("Hall 1", 140))
        ));
        log.info("Seeded theaters and rooms");
    }

    private Theater theater(String name, String location, String contact, Room... rooms) {
        Theater theater = new Theater();
        theater.setName(name);
        theater.setLocation(location);
        theater.setContactInfo(contact);
        for (Room room : rooms) {
            room.setTheater(theater);
            theater.getRooms().add(room);
        }
        return theater;
    }

    private Room room(String name, int capacity) {
        Room room = new Room();
        room.setName(name);
        room.setCapacity(capacity);
        return room;
    }
}
