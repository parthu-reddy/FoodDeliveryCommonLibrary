package com.fooddelivery.common.security.money;

import java.util.Arrays;

public enum MoneyOwnerType {
    CUSTOMER("CUSTOMER"),
    RESTAURANT("RESTAURANT"),
    DRIVER("DRIVER"),
    ADVERTISER("ADVERTISER");

    private final String value;

    MoneyOwnerType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
    
    public static MoneyOwnerType fromString(String text) {
        for (MoneyOwnerType type : MoneyOwnerType.values()) {
            if (type.value.equalsIgnoreCase(text)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown owner type: " + text);
    }
}
