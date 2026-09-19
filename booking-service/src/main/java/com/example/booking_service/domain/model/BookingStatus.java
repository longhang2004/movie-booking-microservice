package com.example.booking_service.domain.model;

public enum BookingStatus {
    PENDING,
    CONFIRMED,
    FAILED,
    CANCELLED;

    public boolean isTerminal() {
        return this == FAILED || this == CANCELLED;
    }

    public BookingStatus transitionTo(BookingStatus target) {
        return switch (this) {
            case PENDING -> switch (target) {
                case CONFIRMED, FAILED, CANCELLED -> target;
                case PENDING -> this;
            };
            case CONFIRMED -> switch (target) {
                case CANCELLED -> target;
                case CONFIRMED -> this;
                case PENDING, FAILED -> throw new IllegalStateException("Cannot move CONFIRMED booking to " + target);
            };
            case FAILED, CANCELLED -> {
                if (this == target) {
                    yield this;
                }
                throw new IllegalStateException("Cannot change terminal booking status " + this);
            }
        };
    }
}
