package com.example.showtime_service.dto;

import lombok.Data;

import java.util.List;

@Data
public class TheaterDTO {
    private Long id;
    private String name;
    private String location;
    private String contactInfo;
    private List<RoomInfo> rooms;

    @Data
    public static class RoomInfo {
        private Long id;
        private String name;
        private int capacity;
    }
}
