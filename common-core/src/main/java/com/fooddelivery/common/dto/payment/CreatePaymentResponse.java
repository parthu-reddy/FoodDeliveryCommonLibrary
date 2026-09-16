package com.fooddelivery.common.dto.payment;

import com.fooddelivery.common.enums.PaymentGateway;

/**
 * Result returned by PaymentGatewayIntegration after it has selected a gateway.
 *
 * <p>The caller must persist both values. Re-deriving the gateway in CustomerApplication would
 * duplicate the payment-routing configuration and can send a later refund to a different gateway.
 */
public record CreatePaymentResponse(String gatewayOrderId, PaymentGateway gateway) {
    public CreatePaymentResponse {
        if (gatewayOrderId == null || gatewayOrderId.isBlank()) {
            throw new IllegalArgumentException("gatewayOrderId is required");
        }
        if (gateway == null) {
            throw new IllegalArgumentException("gateway is required");
        }
    }
}
