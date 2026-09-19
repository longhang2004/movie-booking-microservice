package com.example.payment_service.gateway;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class SimulatedCardGateway implements PaymentGateway {

    @Override
    public ChargeResult charge(ChargeCommand command) {
        if (command.amount() == null || command.amount().compareTo(BigDecimal.ZERO) <= 0) {
            return ChargeResult.declined("invalid_amount");
        }
        return ChargeResult.ok("sim-" + UUID.randomUUID());
    }
}
