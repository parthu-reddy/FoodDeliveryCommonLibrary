package com.fooddelivery.common.enums;

/** The sole permission matrix; platform ADMIN is distinct from organisation ADMIN. */
public enum OrganisationRole {
    OWNER(40),
    ADMIN(30),
    MANAGER(20),
    STAFF(10);

    private final int rank;

    OrganisationRole(int rank) {
        this.rank = rank;
    }

    public int rank() {
        return rank;
    }

    public boolean grants(OrganisationPermission permission) {
        if (permission == null) {
            return false;
        }
        return switch (permission) {
            case ORG_VIEW, STOCK_TOGGLE, ORDERS_OPERATE -> true;
            case ORG_MANAGE -> this == OWNER;
            case MEMBERS_MANAGE, BUSINESS_APPLY, PAYOUTS_MANAGE, WALLET_TOPUP -> rank >= ADMIN.rank;
            case OUTLET_MANAGE, MENU_MANAGE, EARNINGS_VIEW, ADS_VIEW, ADS_MANAGE, WALLET_VIEW ->
                    rank >= MANAGER.rank;
        };
    }
}
