package com.fooddelivery.common.enums;

public enum VehicleClass {
    BICYCLE(10),
    MCWG(20),
    LMV(30),
    EV_TWO_WHEELER(40);

    private final int code;

    VehicleClass(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
