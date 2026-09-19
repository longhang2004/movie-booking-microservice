package com.example.theater_service.controller;

import com.example.theater_service.model.Room;
import com.example.theater_service.model.Theater;
import com.example.theater_service.repository.TheaterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TheaterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TheaterRepository theaterRepository;

    @BeforeEach
    void seedTheater() {
        theaterRepository.deleteAll();
        Theater theater = new Theater();
        theater.setName("CGV Test");
        theater.setLocation("District 1");
        theater.setContactInfo("028-000-0000");
        Room room = new Room();
        room.setName("IMAX");
        room.setCapacity(200);
        room.setTheater(theater);
        theater.getRooms().add(room);
        theaterRepository.save(theater);
    }

    @Test
    void listTheatersIncludesRoomsWithoutLazyInitFailure() throws Exception {
        mockMvc.perform(get("/theaters"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("CGV Test"))
                .andExpect(jsonPath("$[0].rooms", hasSize(1)))
                .andExpect(jsonPath("$[0].rooms[0].name").value("IMAX"));
    }
}
