package com.fooddelivery.common.event;

import java.math.BigDecimal;
import java.util.UUID;
import com.fooddelivery.common.enums.PaymentGateway;
import com.fooddelivery.common.enums.RefundDestination;

@lombok.Data
@lombok.Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public class PaymentRefundedEvent {
    private String orderId;
    private String gatewayOrderId;
    private BigDecimal amountRefunded;
    private PaymentGateway gatewayName;
    private RefundDestination refundDestination;
    private String refundId;
    private String gatewayRefundId;
    private String status;
    private String failureReason;
}
