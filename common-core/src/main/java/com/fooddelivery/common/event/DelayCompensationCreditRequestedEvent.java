package com.fooddelivery.common.event;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * wallet-events / DELAY_COMPENSATION_CREDIT_REQUESTED: OrderService asks WalletService to credit a customer the delay
 * compensation of one delivered order (DelayPenalties Phase 4), written in the transaction that booked the order's DELIVERED
 * ledger command, whose DELAY_PENALTY legs moved the same amount into platform clearing.
 *
 * <p>One credit per order and cause: {@code creditId} is derived from the order id, so a redelivery or a replayed DELIVERED
 * cannot credit twice. Phase 6 sends a second, separately keyed credit when an administrator waives a waiting fee the customer
 * had already paid (D35), funded by its own DELAY_WAIVE ledger transaction. Tolerates the {@code eventType} field the outbox
 * convention repeats in the body.
 *
 * @param creditId   the wallet's reference and idempotency key for this credit
 * @param orderId    the delivered order the compensation is for
 * @param customerId the customer credited
 * @param amount     rupees, two decimals: the sum of the order's penalised R1, R3, R4 and R6 lines, or a waived R5 fee
 * @param rules      the rules the amount sums, by name
 */
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
public record DelayCompensationCreditRequestedEvent(
        @NotNull UUID creditId,
        @NotNull UUID orderId,
        @NotNull UUID customerId,
        @NotNull @Positive BigDecimal amount,
        @NotEmpty List<String> rules
) {
}
