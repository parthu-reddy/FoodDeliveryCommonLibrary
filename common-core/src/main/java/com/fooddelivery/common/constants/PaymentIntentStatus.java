package com.fooddelivery.common.constants;

/**
 * Enum for PaymentIntent statuses.
 * Centralizes all payment status identifiers to prevent inconsistency.
 */
public enum PaymentIntentStatus {
    INITIATED(20),
    SUCCESS(40),
    FAILED(50),
    PENDING_COLLECTION(55),
    COLLECTED(65),
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
