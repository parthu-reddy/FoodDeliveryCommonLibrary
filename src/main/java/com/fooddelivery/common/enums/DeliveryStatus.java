package com.fooddelivery.common.enums;

public enum DeliveryStatus {
    PENDING(10),
    ASSIGNED(20),
    AT_RESTAURANT(30),
    OUT_FOR_DELIVERY(40),
    DELIVERED(50),
    CANCELLED(100),
    FAILED(100);

    private final int sequence;

    DeliveryStatus(int sequence) {
        this.sequence = sequence;
    }

    public int getSequence() {
        return sequence;
    }
}
