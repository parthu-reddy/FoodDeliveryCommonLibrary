package com.fooddelivery.common.constants;

/**
 * Constants for PaymentIntent status strings.
 * Centralizes all payment status identifiers to prevent inconsistency.
 */
public final class PaymentIntentStatus {
    private PaymentIntentStatus() {}

    public static final String CREATED = "CREATED";
    public static final String INITIATED = "INITIATED";
    public static final String PENDING = "PENDING";
    public static final String SUCCESS = "SUCCESS";
    public static final String FAILED = "FAILED";
    public static final String CAPTURED = "CAPTURED";
    public static final String PAID = "PAID";
    public static final String REFUNDED = "REFUNDED";
    public static final String REFUND_FAILED = "REFUND_FAILED";
}
