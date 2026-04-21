package com.example.booking_service.service;

import com.example.booking_service.client.ShowtimeClient;
import com.example.booking_service.exception.ResourceNotFoundException;
import com.example.booking_service.model.Booking;
import com.example.booking_service.repository.BookingRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ShowtimeClient showtimeClient;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public BookingService(BookingRepository bookingRepository,
                          ShowtimeClient showtimeClient,
                          KafkaTemplate<String, String> kafkaTemplate) {
        this.bookingRepository = bookingRepository;
        this.showtimeClient = showtimeClient;
        this.kafkaTemplate = kafkaTemplate;
    }

    public List<Booking> getBookingsByUserId(Long userId) {
        return bookingRepository.findByUserId(userId);
    }

    public Booking createBooking(Long userId, Long showtimeId, List<String> seats) {
        var showtime = showtimeClient.getShowtimeById(showtimeId);

        Booking booking = new Booking();
        booking.setUserId(userId);
        booking.setShowtimeId(showtimeId);
        booking.setSeats(seats);
        booking.setBookingTime(LocalDateTime.now());
        booking.setStatus("PENDING");
        booking.setTotalPrice(showtime.getPrice() * seats.size());

        Booking savedBooking = bookingRepository.save(booking);

        String paymentMessage = String.format(
                "{\"bookingId\": %d, \"userId\": %d, \"amount\": %.2f}",
                savedBooking.getId(), userId, savedBooking.getTotalPrice());
        kafkaTemplate.send("payment-topic", paymentMessage);

        return savedBooking;
    }

    public void cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));
        booking.setStatus("CANCELLED");
        bookingRepository.save(booking);
    }
}
