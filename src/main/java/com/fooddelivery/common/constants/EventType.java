package com.fooddelivery.common.constants;

public enum EventType {
    // Order lifecycle events
    ORDER_CREATED,
    ORDER_PAID,
    ORDER_ACCEPTED,
    ORDER_PREPARING,
    ORDER_READY,
    ORDER_DELIVERED,
    ORDER_REJECTED,
    ORDER_AT_RESTAURANT,
    ORDER_STATUS_UPDATED,
    ORDER_STATUS_SYNC,

    // Cancellation events
    ORDER_CANCELLED,
    ORDER_CANCELLED_BY_RESTAURANT,
    ORDER_CANCELLED_BY_CUSTOMER,

    // Delay approval events
    ORDER_DELAY_APPROVAL_REQUESTED,
    ORDER_DELAY_APPROVED,
    ORDER_DELAY_REJECTED,

    // Dispatch events
    DISPATCH_CANDIDATE_FOUND,
    DISPATCH_FAILED,
    DRIVER_ASSIGNED,
    ORDER_DRIVER_REJECTED,

    // Delivery status values
    DELIVERY_FAILED,

    // Notification events
    NOTIFICATION_REQUEST,

    // Payment events
    PAYMENT_WEBHOOK,
    PAYMENT_COMPLETED,
    PAYMENT_FAILED,
    PAYMENT_REFUNDED,
    PAYMENT_REFUND_REQUESTED,
    OUTLET_ACTIVATED,
    OUTLET_DEACTIVATED,

    // Brand events
    BRAND_CREATED
}
