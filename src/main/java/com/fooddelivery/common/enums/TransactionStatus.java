package com.fooddelivery.common.enums;

public enum TransactionStatus {
    PENDING(10),
    SUCCESS(20),
    FAILED(30);

    private final int code;

    TransactionStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
