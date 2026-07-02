package com.fooddelivery.common.constants;

/**
 * Constants for event type strings used across Kafka messages and outbox events.
 * Centralizes all event type identifiers to prevent typos and ensure consistency.
 */
public final class EventType {
    private EventType() {}

    // Order lifecycle events
    public static final String ORDER_CREATED = "ORDER_CREATED";
    public static final String ORDER_PAID = "ORDER_PAID";
    public static final String ORDER_ACCEPTED = "ORDER_ACCEPTED";
    public static final String ORDER_READY = "ORDER_READY";
    public static final String ORDER_DELIVERED = "ORDER_DELIVERED";
    public static final String ORDER_REJECTED = "ORDER_REJECTED";
    public static final String ORDER_STATUS_UPDATED = "ORDER_STATUS_UPDATED";

    // Cancellation events
    public static final String ORDER_CANCELLED = "ORDER_CANCELLED";
    public static final String ORDER_CANCELLED_BY_RESTAURANT = "ORDER_CANCELLED_BY_RESTAURANT";

    // Delay approval events
    public static final String ORDER_DELAY_APPROVAL_REQUESTED = "ORDER_DELAY_APPROVAL_REQUESTED";
    public static final String ORDER_DELAY_APPROVED = "ORDER_DELAY_APPROVED";
    public static final String ORDER_DELAY_REJECTED = "ORDER_DELAY_REJECTED";

    // Dispatch events
    public static final String DISPATCH_CANDIDATE_FOUND = "DISPATCH_CANDIDATE_FOUND";
    public static final String DISPATCH_FAILED = "DISPATCH_FAILED";
    public static final String DRIVER_ASSIGNED = "DRIVER_ASSIGNED";
    public static final String ORDER_DRIVER_REJECTED = "ORDER_DRIVER_REJECTED";

    // Delivery status values (used inside ORDER_STATUS_UPDATED payloads)
    public static final String DELIVERY_FAILED = "DELIVERY_FAILED";

    // Notification events
    public static final String NOTIFICATION_REQUEST = "NOTIFICATION_REQUEST";

    // Notification template codes (used as templateCode in sendNotification)
    public static final String NOTIFY_DRIVER_ON_THE_WAY = "DRIVER_ON_THE_WAY";
    public static final String NOTIFY_DELAY_APPROVAL_REQUESTED = "DELAY_APPROVAL_REQUESTED";
    public static final String NOTIFY_ORDER_READY_FOR_PICKUP = "ORDER_READY_FOR_PICKUP";
    public static final String NOTIFY_ORDER_CANCELLED_DELAY_TIMEOUT = "ORDER_CANCELLED_DELAY_TIMEOUT";

    // Payment events
    public static final String PAYMENT_WEBHOOK = "PAYMENT_WEBHOOK";
    public static final String PAYMENT_COMPLETED = "PaymentCompletedEvent";
    public static final String PAYMENT_FAILED = "PaymentFailedEvent";
    public static final String PAYMENT_REFUNDED = "PaymentRefundedEvent";
}
