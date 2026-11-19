package com.fooddelivery.common.enums;

/**
 * How the waiter was paid at the table for a {@link TableBillPaymentChannel#COUNTER} payment towards a table bill
 * (TableBill_2026-11-18 D6). Recorded for the restaurant's own till; the platform takes no part in it.
 */
public enum RestaurantCollectionMethod {
    CASH,
    CARD,
    UPI
}
