package com.fooddelivery.common.event;

import java.util.UUID;

public record PaymentFailedEvent(
        UUID orderId,
        String gatewayOrderId,
        String gatewayName,
        String failureReason
) {
}
