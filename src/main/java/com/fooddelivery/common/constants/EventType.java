package com.fooddelivery.common.constants;

public enum EventType {
    // Order lifecycle events
    ORDER_CREATED(10),
    ORDER_PAID(20),
    ORDER_ACCEPTED(30),
    ORDER_PREPARING(40),
    ORDER_READY(50),
    ORDER_DELIVERED(60),
    ORDER_REJECTED(70),
    ORDER_AT_RESTAURANT(80),
    ORDER_STATUS_UPDATED(90),
    ORDER_STATUS_SYNC(100),

    // Cancellation events
    ORDER_CANCELLED(110),
    ORDER_CANCELLED_BY_RESTAURANT(120),
    ORDER_CANCELLED_BY_CUSTOMER(130),

    // Delay approval events
    ORDER_DELAY_APPROVAL_REQUESTED(140),
    ORDER_DELAY_APPROVED(150),
    ORDER_DELAY_REJECTED(160),

    // Dispatch events
    DISPATCH_CANDIDATE_FOUND(170),
    DISPATCH_FAILED(180),
    DRIVER_ASSIGNED(190),
    ORDER_DRIVER_REJECTED(200),

    // Delivery status values
    DELIVERY_FAILED(210),

    // Notification events
    NOTIFICATION_REQUEST(220),

    // Payment events
    PAYMENT_WEBHOOK(230),
    PAYMENT_COMPLETED(240),
    PAYMENT_FAILED(250),
    PAYMENT_REFUNDED(260),
    PAYMENT_REFUND_REQUESTED(270),
    PAYMENT_PARTIALLY_REFUNDED(275),
    ORDER_PARTIALLY_REFUNDED(276),
    
    // Ledger events
    LEDGER_TRANSACTION_REQUEST(280),
    
    OUTLET_ACTIVATED(290),
    OUTLET_DEACTIVATED(300),

    // Brand events
    BRAND_CREATED(310);

    private final int code;

    EventType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
