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
    DELAY_PENALTY;
}
