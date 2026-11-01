package com.fooddelivery.common.enums;

/**
 * How the restaurant was paid at the table for a {@link DineInSettlement#RESTAURANT} order (D7). Recorded for the
 * restaurant's own till; the platform takes no part in it.
 */
public enum RestaurantCollectionMethod {
    CASH,
    CARD,
    UPI
}
