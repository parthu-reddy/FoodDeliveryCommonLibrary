package com.fooddelivery.common.enums;

public enum ChargeCategory {
    DELIVERY_FEE,
    PLATFORM_FIXED_FEE,
    PLATFORM_BONUS,
    FOOD_COST,
    SGST,
    CGST,
    REFUND,
    ORDER_TOTAL,
    AD_IMPRESSION,
    AD_CLICK,
    AD_CONVERSION,
    BUSINESS_WALLET_TOPUP,
    CLAWBACK,
    PAYOUT_TRANSFER,
    STORE_CREDIT,
    /** A delay penalty (DelayPenalties Phase 4): the late party pays the party who waited, inside the order's DELIVERED transaction. */
    DELAY_PENALTY,
    /** A booking deposit captured: gateway receivable to the held account (DiningPickupBooking D50, as all five below). */
    RESERVATION_DEPOSIT,
    /** Booking credit spent on a served order: held to clearing (APP order) or to the restaurant's payable (RESTAURANT order). */
    BOOKING_CREDIT,
    /** Credit a visit left unspent: held to the restaurant, the platform's commission and GST. */
    MIN_SPEND_SHORTFALL,
    /** A deposit forfeited by a no-show or a late cancellation: held to the restaurant, the platform's commission and GST. */
    NO_SHOW_FORFEIT,
    /** A deposit given back: held to the gateway receivable. */
    RESERVATION_REFUND;
}
