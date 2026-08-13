package com.fooddelivery.common.enums;

public enum AccountType {
    CUSTOMER(10),
    PLATFORM(20),
    RESTAURANT(30),
    DRIVER(40),
    ADVERTISER_WALLET(50),
    GOVERNMENT(60);

    private final int code;

    AccountType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
