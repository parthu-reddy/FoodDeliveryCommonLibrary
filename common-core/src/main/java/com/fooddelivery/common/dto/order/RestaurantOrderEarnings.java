package com.fooddelivery.common.dto.order;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.Data;

@Data
public class RestaurantOrderEarnings {
    private UUID orderId;
    private UUID restaurantId;
    private BigDecimal foodCost;
    private BigDecimal platformFee;
    private BigDecimal deliveryContribution;
    private BigDecimal netPayout;
}
