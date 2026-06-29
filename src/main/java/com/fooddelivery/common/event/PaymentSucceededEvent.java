package com.fooddelivery.common.event;

import java.math.BigDecimal;

public record PaymentSucceededEvent(
    String orderId,
    String gatewayOrderId,
    BigDecimal amount,
    String gatewayName
) {}
