package com.fooddelivery.common.enums;

public enum PaymentGateway {
    RAZORPAY(10),
    CASHFREE(20),
    VYAPAR(30);

    private final int code;

    PaymentGateway(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
