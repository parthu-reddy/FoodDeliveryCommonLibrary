package com.fooddelivery.common.enums;

/**
 * How a guest came to sit at a dine-in table: {@code dining_session_guests.joined_via} (DiningPickupBooking D24). The
 * floor shows a session as self-seated when it has no waiter and its guests joined other than via STAFF.
 */
public enum DiningJoinVia {
    /** A waiter seated the guest (the existing seat-by-phone flow). */
    STAFF,
    /** The guest scanned the table's QR, whose token proves presence (D22, D23). */
    QR,
    /** The guest typed the table number, and the device's location was inside the outlet geofence (D22). */
    LOCATION,
    /** The guest typed the table number and the 4-digit code printed beside it (D22, D23). */
    TABLE_CODE
}
