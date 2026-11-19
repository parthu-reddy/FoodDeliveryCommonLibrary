package com.fooddelivery.common.constants;

/**
 * {@code LedgerLeg.authorizedBy} values the ledger's balance rule recognises by name (DineInPhoneAndCounter D9). A CLAWBACK
 * leg needs any authorisation to take a payable negative; a PLATFORM_FIXED_FEE leg only {@link #TABLE_BILL_COUNTER_SHARE}.
 * A settled table bill is booked once, each payment allocated to its rounds pro rata (TableBill_2026-11-18 D9).
 */
public final class LedgerAuthorisations {

    /** A round's share of a table-bill payment a guest made in the app: the platform holds it and pays the restaurant. */
    public static final String TABLE_BILL_APP_SHARE = "TABLE_BILL_APP_SHARE";

    /**
     * A round's share of a table-bill payment the waiter recorded at the counter: the platform never held it, so the
     * restaurant's fee is debited from its payable even below zero and netted from its next payout.
     */
    public static final String TABLE_BILL_COUNTER_SHARE = "TABLE_BILL_COUNTER_SHARE";

    private LedgerAuthorisations() {
    }
}
