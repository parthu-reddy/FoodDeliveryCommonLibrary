package com.fooddelivery.common.event;

import java.math.BigDecimal;
import java.util.UUID;

@lombok.Data
@lombok.Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public class OrderCreatedEvent {
    private UUID orderId;
    private UUID customerId;
    private UUID restaurantId;
    private BigDecimal totalAmount;
    private Double deliveryLat;
    private Double deliveryLng;
    private String deliveryAddress;
    private String pickupOtp;
    private String deliveryOtp;
}
