package com.example.payment_service.gateway;

import java.math.BigDecimal;

public record ChargeCommand(Long bookingId, Long userId, BigDecimal amount) {
}
