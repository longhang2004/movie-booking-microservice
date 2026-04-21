package com.example.booking_service.service;

import com.example.booking_service.client.ShowtimeClient;
import com.example.booking_service.dto.ShowtimeDTO;
import com.example.booking_service.exception.ResourceNotFoundException;
import com.example.booking_service.model.Booking;
import com.example.booking_service.repository.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ShowtimeClient showtimeClient;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @InjectMocks
    private BookingService bookingService;

    private ShowtimeDTO showtime;
    private Booking booking;

    @BeforeEach
    void setUp() {
        showtime = new ShowtimeDTO();
        showtime.setId(1L);
        showtime.setPrice(150000.0);

        booking = new Booking();
        booking.setId(1L);
        booking.setUserId(10L);
        booking.setShowtimeId(1L);
        booking.setSeats(List.of("A1", "A2"));
        booking.setStatus("PENDING");
        booking.setTotalPrice(300000.0);
    }

    @Test
    void createBooking_validRequest_createsBookingAndPublishesKafkaEvent() {
        when(showtimeClient.getShowtimeById(1L)).thenReturn(showtime);
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        Booking result = bookingService.createBooking(10L, 1L, List.of("A1", "A2"));

        assertThat(result.getStatus()).isEqualTo("PENDING");
        assertThat(result.getTotalPrice()).isEqualTo(300000.0);
        verify(kafkaTemplate, times(1)).send(eq("payment-topic"), anyString());
    }

    @Test
    void getBookingsByUserId_returnsBookingsForUser() {
        when(bookingRepository.findByUserId(10L)).thenReturn(List.of(booking));

        List<Booking> result = bookingService.getBookingsByUserId(10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(10L);
    }

    @Test
    void cancelBooking_existingId_setsStatusCancelled() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        bookingService.cancelBooking(1L);

        assertThat(booking.getStatus()).isEqualTo("CANCELLED");
        verify(bookingRepository, times(1)).save(booking);
    }

    @Test
    void cancelBooking_nonExistingId_throwsResourceNotFoundException() {
        when(bookingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.cancelBooking(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Booking not found with id: 99");
    }
}
