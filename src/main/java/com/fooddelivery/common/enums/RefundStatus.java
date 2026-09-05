package com.fooddelivery.common.enums;

public enum RefundStatus {
    REQUESTED(10),
    PROCESSING(20),
    COMPLETED(30),
    FAILED(40),
    CANCELLED(50);

    private final int code;

    RefundStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
