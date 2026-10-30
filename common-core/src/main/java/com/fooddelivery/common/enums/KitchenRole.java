package com.fooddelivery.common.enums;

/**
 * A person's role in one outlet's kitchen (kitchen staff portal). Not {@link OrganisationRole}: a kitchen MANAGER runs the
 * pass at one outlet and holds no organisation power. Shared because identity reads it from KitchenStaffChangedEvent.
 */
public enum KitchenRole {
    MANAGER(10),
    CHEF(20),
    /** Works the floor of one outlet: takes dine-in orders at tables and serves them (WaiterProfile_2026-10-29). */
    WAITER(30);

    private final int code;

    KitchenRole(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
