package com.fooddelivery.common.enums;

public enum LedgerAccountType {
    GATEWAY_RECEIVABLE(Kind.EXTERNAL),
    BANK(Kind.EXTERNAL),
    PLATFORM_CLEARING(Kind.INTERNAL),
    PLATFORM_REVENUE(Kind.INTERNAL),
    TAX_PAYABLE(Kind.INTERNAL),
    PAYOUT_IN_TRANSIT(Kind.INTERNAL),
    RESTAURANT_PAYABLE(Kind.PAYABLE),
    DRIVER_PAYABLE(Kind.PAYABLE),
    CUSTOMER_CREDIT(Kind.PREPAID),
    ADVERTISER_PREPAID(Kind.PREPAID);

    private final Kind kind;

    LedgerAccountType(Kind kind) {
        this.kind = kind;
    }

    public Kind getKind() {
        return kind;
    }

    public enum Kind {
        EXTERNAL, INTERNAL, PAYABLE, PREPAID
    }
}
