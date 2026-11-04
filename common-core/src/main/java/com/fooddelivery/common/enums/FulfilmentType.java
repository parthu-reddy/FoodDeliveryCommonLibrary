package com.fooddelivery.common.enums;

/**
 * How an order reaches the guest. DELIVERY: a rider carries it to an address (dispatch scope, OTPs, delivery fee).
 * DINE_IN: a waiter takes it at a table in the outlet and it completes when served (no rider, no delivery fee, no delay
 * penalties; RandomDocuments/WaiterProfile_2026-10-29). PICKUP: the customer collects it at the outlet counter (takeaway)
 * and shows a 4-digit handover code; no address, coordinates, dispatch scope, rider, table, tip or delay dues
 * (RandomDocuments/DiningPickupBooking_2026-11-03 D9). Consumers branch on this value with an exhaustive switch, never on
 * a missing dispatch field.
 */
public enum FulfilmentType {
    DELIVERY(10),
    DINE_IN(20),
    PICKUP(30);

    private final int code;

    FulfilmentType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
