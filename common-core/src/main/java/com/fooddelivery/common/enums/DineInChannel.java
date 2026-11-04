package com.fooddelivery.common.enums;

/**
 * Who placed a dine-in order: {@code orders.dine_in_channel} (DiningPickupBooking D25). STAFF orders carry the placing
 * waiter's staff id and GUEST orders carry none; a CHECK on each table holds that rule.
 */
public enum DineInChannel {
    /** A waiter placed it for the guest. */
    STAFF,
    /** The seated guest placed it from their own phone. */
    GUEST
}
