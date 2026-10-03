package com.fooddelivery.common.enums;

/** Permissions shared by every organisation-scoped service. */
public enum OrganisationPermission {
    ORG_VIEW(true), ORG_MANAGE(false), MEMBERS_MANAGE(false), BUSINESS_APPLY(false),
    OUTLET_MANAGE(false), MENU_MANAGE(false), STOCK_TOGGLE(false), ORDERS_OPERATE(false),
    EARNINGS_VIEW(true), PAYOUTS_MANAGE(false), ADS_VIEW(true), ADS_MANAGE(false),
    WALLET_VIEW(true), WALLET_TOPUP(false);

    private final boolean readOnly;

    OrganisationPermission(boolean readOnly) { this.readOnly = readOnly; }

    public boolean readOnly() { return readOnly; }
}
