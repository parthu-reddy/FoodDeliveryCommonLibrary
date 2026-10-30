package com.fooddelivery.common.enums;

/**
 * How an order reaches the guest. DELIVERY: a rider carries it to an address (dispatch scope, OTPs, delivery fee).
 * DINE_IN: a waiter takes it at a table in the outlet and it completes when served (no rider, no delivery fee, no delay
 * penalties; RandomDocuments/WaiterProfile_2026-10-29). Consumers branch on this value, never on a missing dispatch field.
 */
public enum FulfilmentType {
    DELIVERY(10),
    DINE_IN(20);

    private final int code;

    FulfilmentType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
