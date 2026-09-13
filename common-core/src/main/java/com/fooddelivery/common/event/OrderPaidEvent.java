package com.fooddelivery.common.event;

import java.util.UUID;

@lombok.Data
@lombok.Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public class OrderPaidEvent {
    private UUID orderId;
    private UUID restaurantId;
    /**
     * Absent until 2026-09-10, so RestaurantOrder.customerId was permanently NULL for every order
     * the platform had ever taken.
     */
    private UUID customerId;
    private String customerName;
    /**
     * How the customer paid.
     *
     * <p>ORDER_PAID and ORDER_PLACED_COD were consumed by the same handler, which recorded neither
     * the event type nor this field -- so a restaurant could not tell an order the rider must
     * collect cash for from one already paid, and the delivery service could not require a declared
     * amount at handover.
     */
    private com.fooddelivery.common.enums.PaymentMethod paymentMethod;
    private Integer estimatedPrepTimeMinutes;
    private Double deliveryLat;
    private Double deliveryLng;
    private String deliveryAddress;
    private String itemsJson;
    private String pickupOtp;
    private String deliveryOtp;
    private java.math.BigDecimal totalAmount;
    private java.math.BigDecimal itemTotal;
    private java.math.BigDecimal restaurantPlatformFee;
    private java.math.BigDecimal restaurantDeliveryContribution;
    private java.math.BigDecimal platformBonus;
    private java.math.BigDecimal restaurantPayout;
}
