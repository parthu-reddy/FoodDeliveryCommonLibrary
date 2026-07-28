package com.fooddelivery.common.enums;

public enum RoleName {
    CUSTOMER(10),
    DELIVERY(20),
    RESTAURANT(30),
    ADMIN(40);

    private final int code;

    RoleName(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
