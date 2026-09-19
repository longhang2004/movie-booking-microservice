package com.example.payment_service.gateway;

public record ChargeResult(boolean success, String providerReference, String message) {

    public static ChargeResult ok(String reference) {
        return new ChargeResult(true, reference, "approved");
    }

    public static ChargeResult declined(String message) {
        return new ChargeResult(false, null, message);
    }
}
