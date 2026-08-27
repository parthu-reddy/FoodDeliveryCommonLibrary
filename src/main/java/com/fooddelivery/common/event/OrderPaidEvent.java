package com.fooddelivery.common.event;

import java.util.UUID;

@lombok.Data
@lombok.Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public class OrderPaidEvent {
    private UUID orderId;
    private UUID restaurantId;
    private String customerName;
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
