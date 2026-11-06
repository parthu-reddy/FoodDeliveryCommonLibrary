package com.fooddelivery.common.event;

/**
 * A business wallet top-up whose payment the provider declined or that failed: WalletService marks the
 * top-up FAILED with the reason and credits nothing.
 *
 * <p>Its own shape because {@link PaymentFailedEvent} is about an order. {@code orderId} here is the top-up's id,
 * the reference WalletService gave the gateway. Ignores unknown fields for the same reason the payment events do: the body
 * repeats {@code eventType}.
 */
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
public record BusinessWalletTopupFailedEvent(
        String orderId,
        String gatewayOrderId,
        String gatewayName,
        String failureReason
) {
}
