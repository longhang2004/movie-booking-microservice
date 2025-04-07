package com.example.booking_service.service;

import com.example.booking_service.client.ShowtimeClient;
import com.example.booking_service.model.Booking;
import com.example.booking_service.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
// import java.util.Optional;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ShowtimeClient showtimeClient;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public List<Booking> getBookingsByUserId(Long userId) {
        return bookingRepository.findByUserId(userId);
    }

    public Booking createBooking(Long userId, Long showtimeId, List<String> seats) {
        // Kiểm tra suất chiếu có hợp lệ không
        showtimeClient.getShowtimeById(showtimeId);

        Booking booking = new Booking();
        booking.setUserId(userId);
        booking.setShowtimeId(showtimeId);
        booking.setSeats(seats);
        booking.setBookingTime(LocalDateTime.now());
        booking.setStatus("PENDING");
        booking.setTotalPrice(calculateTotalPrice(showtimeId, seats.size()));

        Booking savedBooking = bookingRepository.save(booking);

        // Gửi thông báo đến payment-service qua Kafka
        String paymentMessage = String.format("{\"bookingId\": %d, \"userId\": %d, \"amount\": %.2f}",
                savedBooking.getId(), userId, savedBooking.getTotalPrice());
        kafkaTemplate.send("payment-topic", paymentMessage);

        return savedBooking;
    }

    public void cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        booking.setStatus("CANCELLED");
        bookingRepository.save(booking);
    }

    private double calculateTotalPrice(Long showtimeId, int numberOfSeats) {
        // Giả sử lấy giá từ showtime-service (cần triển khai logic thực tế)
        double pricePerSeat = showtimeClient.getShowtimeById(showtimeId).getPrice();
        return pricePerSeat * numberOfSeats;
    }
}