package com.example.booking_service.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Booking {

    private Long id;
    private Long userId;
    private Long showtimeId;
    private List<String> seats;
    private BookingStatus status;
    private BigDecimal totalPrice;
    private String idempotencyKey;
    private Instant createdAt;
    private Instant updatedAt;
    private Long version;

    public static Booking create(Long userId, Long showtimeId, List<String> seats, BigDecimal totalPrice, String idempotencyKey) {
        Booking booking = new Booking();
        booking.userId = Objects.requireNonNull(userId);
        booking.showtimeId = Objects.requireNonNull(showtimeId);
        booking.seats = List.copyOf(seats);
        booking.status = BookingStatus.PENDING;
        booking.totalPrice = totalPrice;
        booking.idempotencyKey = idempotencyKey;
        booking.createdAt = Instant.now();
        booking.updatedAt = booking.createdAt;
        booking.version = 0L;
        return booking;
    }

    public static Booking restore(Long id, Long userId, Long showtimeId, List<String> seats, BookingStatus status,
                                  BigDecimal totalPrice, String idempotencyKey, Instant createdAt, Instant updatedAt, Long version) {
        Booking booking = new Booking();
        booking.id = id;
        booking.userId = userId;
        booking.showtimeId = showtimeId;
        booking.seats = new ArrayList<>(seats);
        booking.status = status;
        booking.totalPrice = totalPrice;
        booking.idempotencyKey = idempotencyKey;
        booking.createdAt = createdAt;
        booking.updatedAt = updatedAt;
        booking.version = version;
        return booking;
    }

    public void confirm() {
        this.status = status.transitionTo(BookingStatus.CONFIRMED);
        this.updatedAt = Instant.now();
    }

    public void fail() {
        this.status = status.transitionTo(BookingStatus.FAILED);
        this.updatedAt = Instant.now();
    }

    public void cancel() {
        this.status = status.transitionTo(BookingStatus.CANCELLED);
        this.updatedAt = Instant.now();
    }

    public void assignId(Long id) {
        this.id = id;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Long getShowtimeId() { return showtimeId; }
    public List<String> getSeats() { return seats; }
    public BookingStatus getStatus() { return status; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Long getVersion() { return version; }
}
