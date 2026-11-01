package com.fooddelivery.common.constants;

/**
 * {@code LedgerLeg.authorizedBy} values the ledger's balance rule recognises by name (DineInPhoneAndCounter D9). A CLAWBACK
 * leg needs any authorisation to take a payable negative; a PLATFORM_FIXED_FEE leg only this one.
 */
public final class LedgerAuthorisations {

    /**
     * A dine-in order the guest paid at the restaurant: the platform never held the bill, so the restaurant's fee is
     * debited from its payable even below zero and netted from its next payout.
     */
    public static final String DINE_IN_RESTAURANT_SETTLED = "DINE_IN_RESTAURANT_SETTLED";

    private LedgerAuthorisations() {
    }
}
