package com.fooddelivery.common.constants;

public enum AggregateType {
    ORDER(10),
    PAYMENT(20),
    NOTIFICATION(30),
    OUTLET(40),
    BRAND(50),
    LEDGER(60),
    ADVERTISEMENT(70),
    WALLET(80);

    private final int code;

    AggregateType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
