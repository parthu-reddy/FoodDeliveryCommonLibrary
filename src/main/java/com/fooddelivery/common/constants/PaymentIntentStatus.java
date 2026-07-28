package com.fooddelivery.common.constants;

/**
 * Enum for PaymentIntent statuses.
 * Centralizes all payment status identifiers to prevent inconsistency.
 */
public enum PaymentIntentStatus {
    CREATED(10),
    INITIATED(20),
    PENDING(30),
    SUCCESS(40),
    FAILED(50),
    CAPTURED(60),
    PAID(70),
    PARTIALLY_REFUNDED(80),
    REFUNDED(90),
    REFUND_PENDING(100),
    REFUND_FAILED(110);

    private final int code;

    PaymentIntentStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
