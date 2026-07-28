package com.fooddelivery.common.enums;

public enum ChannelType {
    SMS(10),
    EMAIL(20),
    PUSH(30),
    WHATSAPP(40);

    private final int code;

    ChannelType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
