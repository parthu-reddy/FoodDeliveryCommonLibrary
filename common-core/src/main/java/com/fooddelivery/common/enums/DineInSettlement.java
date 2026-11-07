package com.fooddelivery.common.enums;

/**
 * How a dine-in order is settled (DineInPhoneAndCounter_2026-10-31 D5). Never a {@link PaymentMethod}: that stays the
 * platform's prepaid methods, and delivery accepts nothing else.
 */
public enum DineInSettlement {
    /** The guest pays in the app (a {@link PaymentMethod}); the kitchen starts once the payment succeeds. */
    APP,
    /**
     * The guest pays the restaurant at the table; the platform never holds the money. The kitchen starts when the waiter
     * places the order, the waiter records the payment ({@link RestaurantCollectionMethod}), and the restaurant owes the
     * platform fee from its next payout.
     */
    RESTAURANT,
    /**
     * A booking's prepaid credit covers the whole order (DiningPickupBooking D41), so no gateway takes any money for it. The
     * kitchen starts in the placing transaction. Order-service decides it; no caller may ask for it.
     */
    CREDIT;

    /**
     * The one rule every order event with a payment method carries (OrderPaidEvent, OrderAcceptedEvent): a DELIVERY or
     * PICKUP order is prepaid in the app, so it has a payment method and no settlement; a DINE_IN order has a settlement,
     * and a payment method exactly when APP. A missing fulfilment type is left to the event's own {@code @NotNull}.
     */
    public static boolean consistent(FulfilmentType fulfilmentType, PaymentMethod paymentMethod, DineInSettlement settlement) {
        if (fulfilmentType == null) {
            return true;
        }
        return switch (fulfilmentType) {
            case DELIVERY, PICKUP -> settlement == null && paymentMethod != null;
            case DINE_IN -> settlement != null && (paymentMethod != null) == (settlement == APP);
        };
    }
}
