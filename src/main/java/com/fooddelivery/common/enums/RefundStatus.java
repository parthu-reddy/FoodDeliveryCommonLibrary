package com.fooddelivery.common.enums;

public enum RefundStatus {
    PENDING(10),
    COMPLETED(20),
    FAILED(30);

    private final int code;

    RefundStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
