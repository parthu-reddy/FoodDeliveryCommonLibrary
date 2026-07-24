package com.fooddelivery.common.enums;

public enum OrderStatus {
    CREATED(10),
    PENDING_ACCEPTANCE(20),
    AWAITING_DELAY_APPROVAL(30),
    ACCEPTED(40),
    PREPARING(50),
    READY_FOR_PICKUP(60),
    PICKED_UP(70),
    DELIVERED(90),
    CANCELLED(100),
    CANCELLED_BY_RESTAURANT(100),
    DELIVERY_FAILED(100);

    private final int sequence;

    OrderStatus(int sequence) {
        this.sequence = sequence;
    }

    public int getSequence() {
        return sequence;
    }
}
