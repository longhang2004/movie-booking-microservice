package com.example.booking_service.config;

import com.example.booking_service.model.Booking;
import com.example.booking_service.repository.BookingRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class BookingListener {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @KafkaListener(topics = "booking-update-topic", groupId = "booking-group")
    public void listen(String message) {
        try {
            JsonNode jsonNode = objectMapper.readTree(message);
            Long bookingId = jsonNode.get("bookingId").asLong();
            String status = jsonNode.get("status").asText();

            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking not found: " + bookingId));
            booking.setStatus(status);
            bookingRepository.save(booking);

            System.out.println("Updated booking " + bookingId + " to status " + status);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}