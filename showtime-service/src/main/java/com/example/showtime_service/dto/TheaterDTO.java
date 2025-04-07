package com.example.showtime_service.dto;

import lombok.Data;

import java.util.List;

import com.example.showtime_service.model.Room;

@Data
public class TheaterDTO {

    private Long id;
    private String name;
    private String location;
    private String contactInfo;
    private List<Room> rooms;

    // Constructors, getters, setters
}