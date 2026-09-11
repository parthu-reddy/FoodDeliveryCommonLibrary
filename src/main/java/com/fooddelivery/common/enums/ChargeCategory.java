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
    AD_WALLET_TOPUP,
    CLAWBACK,
    PAYOUT_TRANSFER,
    CASH_COLLECTED,
    /**
     * The gap between what a COD order was worth and what the rider says they collected.
     *
     * <p>Booked against the rider's earnings, because that is the only account it can come from.
     * Before this the collected leg was always the order total regardless of what the rider
     * declared, so a short collection was invisible and the book balanced on a number nobody had
     * checked.
     */
    CASH_SHORTFALL,
    CASH_REMITTED,
    STORE_CREDIT;
}
