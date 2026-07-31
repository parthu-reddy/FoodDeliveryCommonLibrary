package com.fooddelivery.common.enums;

public enum OrderStatus {
    CREATED(10),
    PENDING_ACCEPTANCE(20),
    AWAITING_DELAY_APPROVAL(30),
    ACCEPTED(40),
    PREPARING(50),
    READY_FOR_PICKUP(60),
    HANDED_OVER(70),
    CANCELLED(100),
    CANCELLED_BY_RESTAURANT(100);

    private final int sequence;

    OrderStatus(int sequence) {
        this.sequence = sequence;
    }

    public int getSequence() {
        return sequence;
    }
}
