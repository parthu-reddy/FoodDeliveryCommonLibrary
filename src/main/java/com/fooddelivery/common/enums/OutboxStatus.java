package com.fooddelivery.common.enums;

public enum OutboxStatus {
    UNPROCESSED(10),
    PROCESSED(30),
    FAILED(40),
    DLQ(50);

    private final int code;

    OutboxStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
