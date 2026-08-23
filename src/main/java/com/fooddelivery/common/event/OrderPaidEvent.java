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




























}
