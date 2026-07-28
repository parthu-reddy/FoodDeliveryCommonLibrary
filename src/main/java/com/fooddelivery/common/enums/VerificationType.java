package com.fooddelivery.common.enums;

public enum VerificationType {
    GSTIN(10),
    PENNY_DROP(20),
    PAN(30),
    DRIVING_LICENSE(40),
    RC(50);

    private final int code;

    VerificationType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
