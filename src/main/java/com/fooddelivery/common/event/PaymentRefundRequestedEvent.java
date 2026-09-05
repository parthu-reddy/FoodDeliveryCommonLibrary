package com.fooddelivery.common.event;

import java.math.BigDecimal;
import com.fooddelivery.common.enums.PaymentGateway;

@lombok.Data
@lombok.Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public class PaymentRefundRequestedEvent {
    private String refundId;
    private String orderId;
    private String gatewayOrderId;
    private BigDecimal amount;
    private PaymentGateway gatewayName;
}
