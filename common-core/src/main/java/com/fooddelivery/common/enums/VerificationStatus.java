package com.fooddelivery.common.enums;

public enum VerificationStatus {
    PENDING(10),
    APPROVED(20),
    VERIFIED(30),
    REJECTED(40),
    MANUAL_REVIEW(50),
    FAILED(60);

    private final int code;

    VerificationStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
