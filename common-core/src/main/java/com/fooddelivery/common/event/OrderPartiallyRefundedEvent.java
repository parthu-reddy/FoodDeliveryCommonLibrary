package com.fooddelivery.common.event;

import java.util.UUID;
import java.math.BigDecimal;

@lombok.Data
@lombok.Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
public class OrderPartiallyRefundedEvent implements OrderScopedEvent {
    private String orderId;
    private BigDecimal amount;
    private String reason;

    @Override
    public UUID orderUuid() {
        return orderId == null ? null : UUID.fromString(orderId);
    }
}
