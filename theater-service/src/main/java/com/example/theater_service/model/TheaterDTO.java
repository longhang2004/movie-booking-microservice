package com.example.theater_service.model;

import lombok.Data;

import java.util.List;

@Data
public class TheaterDTO {

    private Long id;
    private String name;
    private String location;
    private String contactInfo;
    private List<Room> rooms;

    // Constructors, getters, setters
}