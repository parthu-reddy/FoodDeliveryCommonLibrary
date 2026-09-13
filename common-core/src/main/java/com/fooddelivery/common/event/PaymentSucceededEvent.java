package com.fooddelivery.common.event;

import java.math.BigDecimal;
import java.time.Instant;
import com.fooddelivery.common.enums.PaymentMethod;

public record PaymentSucceededEvent(
    String orderId,
    String gatewayOrderId,
    BigDecimal amount,
    String gatewayName,
    PaymentMethod paymentMethod,
    Instant paidAt
) {}
