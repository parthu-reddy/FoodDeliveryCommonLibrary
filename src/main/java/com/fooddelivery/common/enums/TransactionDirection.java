package com.fooddelivery.common.enums;

public enum TransactionDirection {
    CREDIT(10),
    DEBIT(20);

    private final int code;

    TransactionDirection(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
