package com.fooddelivery.common.enums;

/** How one payment towards a table bill was taken (TableBill_2026-11-18 D6). */
public enum TableBillPaymentChannel {
    /** The waiter recorded it ({@link RestaurantCollectionMethod}); the platform never held the money. */
    COUNTER,
    /** A guest paid it in the app, through the gateway, with purpose {@link PaymentPurpose#TABLE_BILL}. */
    APP
}
