package com.fooddelivery.common.constants;

public enum NotificationTemplate {
    DRIVER_ON_THE_WAY(10),
    DELAY_APPROVAL_REQUESTED(20),
    ORDER_READY_FOR_PICKUP(30),
    ORDER_CANCELLED_DELAY_TIMEOUT(40),
    NEW_ORDER_DISPATCH(50),
    ORDER_ASSIGNED(60),
    OTP_LOGIN(70);

    private final int code;

    NotificationTemplate(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
