package com.fooddelivery.common.enums;

/**
 * What a payment is for (DiningPickupBooking D48). Every create-order caller names it, and the gateway stores it on the intent
 * and chooses each outcome's event by an exhaustive switch over it, so a new purpose does not compile until it has its events.
 */
public enum PaymentPurpose {
    ORDER,
    BUSINESS_WALLET_TOPUP,
    RESERVATION_DEPOSIT
}
