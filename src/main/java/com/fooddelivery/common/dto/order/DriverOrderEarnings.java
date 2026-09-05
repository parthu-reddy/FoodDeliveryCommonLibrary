package com.fooddelivery.common.dto.order;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.Data;

@Data
public class DriverOrderEarnings {
    private UUID orderId;
    private UUID driverId;
    private BigDecimal grossPayout;
    private BigDecimal taxes;
    private BigDecimal netPayout;
    private BigDecimal customerContribution;
    private BigDecimal restaurantContribution;
    private BigDecimal platformBonus;
}
