package com.fooddelivery.common.enums;

/**
 * The one rule every order event with a payment method carries (OrderPaidEvent, OrderAcceptedEvent; TableBill_2026-11-18
 * D4): a DELIVERY or PICKUP order is prepaid in the app, so it has a {@link PaymentMethod}; a DINE_IN order is a round on
 * its table's bill and is never paid on its own, so it has none. The bill's payments carry their own method.
 */
public final class PaymentMethodRule {

    private PaymentMethodRule() {
    }

    /** A missing fulfilment type is left to the event's own {@code @NotNull}. */
    public static boolean consistent(FulfilmentType fulfilmentType, PaymentMethod paymentMethod) {
        if (fulfilmentType == null) {
            return true;
        }
        return switch (fulfilmentType) {
            case DELIVERY, PICKUP -> paymentMethod != null;
            case DINE_IN -> paymentMethod == null;
        };
    }
}
