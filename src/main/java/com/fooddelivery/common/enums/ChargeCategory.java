package com.fooddelivery.common.enums;

public enum ChargeCategory {
    DELIVERY_FEE(10),
    PLATFORM_FIXED_FEE(20),
    PLATFORM_BONUS(30),
    FOOD_COST(40),
    TIP(50),
    PACKAGING_FEE(60),
    SURGE_PRICING(70),
    TAX(80),
    SGST(81),
    CGST(82),
    REFUND(90),
    ORDER_TOTAL(100),
    PAYOUT(110);

    private final int code;

    ChargeCategory(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
