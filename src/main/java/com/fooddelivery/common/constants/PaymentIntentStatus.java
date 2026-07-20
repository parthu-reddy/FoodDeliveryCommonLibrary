package com.fooddelivery.common.constants;

/**
 * Enum for PaymentIntent statuses.
 * Centralizes all payment status identifiers to prevent inconsistency.
 */
public enum PaymentIntentStatus {
    CREATED,
    INITIATED,
    PENDING,
    SUCCESS,
    FAILED,
    CAPTURED,
    PAID,
    PARTIALLY_REFUNDED,
    REFUNDED,
    REFUND_FAILED
}
