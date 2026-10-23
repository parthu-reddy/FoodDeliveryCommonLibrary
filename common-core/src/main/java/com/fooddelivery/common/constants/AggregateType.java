package com.fooddelivery.common.constants;

public enum AggregateType {
    ORDER(10),
    PAYMENT(20),
    NOTIFICATION(30),
    OUTLET(40),
    BRAND(50),
    LEDGER(60),
    ADVERTISEMENT(70),
    WALLET(80),
    CHAT_SESSION(90),
    REVIEW(100),
    ORGANISATION(110),
    DELIVERY_PARTNER(120),
    // A brand's menu (items, overrides, stock, prep default): MENU_UPDATED, read by the customer menu cache.
    MENU(130),
    // An outlet's kitchen staff row: KITCHEN_STAFF_CHANGED, read by identity for the KITCHEN role.
    KITCHEN_STAFF(140);

    private final int code;

    AggregateType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
