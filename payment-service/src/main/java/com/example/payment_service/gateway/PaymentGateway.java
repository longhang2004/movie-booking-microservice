package com.example.payment_service.gateway;

public interface PaymentGateway {
    ChargeResult charge(ChargeCommand command);
}
