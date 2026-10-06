package com.fooddelivery.common.event;

/**
 * A business wallet top-up whose payment the provider declined or that failed: WalletService marks the
 * top-up FAILED with the reason and credits nothing.
 *
 * <p>Its own shape because {@link PaymentFailedEvent} types {@code orderId} as a UUID (an order), and a
 * top-up's internal order id is {@code WALLET_<topupId>}. Ignores unknown fields
 * for the same reason the payment events do: the body repeats {@code eventType}.
 */
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
public record BusinessWalletTopupFailedEvent(
        String orderId,
        String gatewayOrderId,
        String gatewayName,
        String failureReason
) {
}
