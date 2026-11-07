package com.fooddelivery.common.event;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

/**
 * Every non-void line of a DINE_IN order was served (order-events, key = orderId). RestaurantApplication publishes it
 * from the serve transition; OrderService completes the order without a rider and books the ledger; the kitchen closes
 * the ticket. {@code restaurantId} is the outlet, as on every other order event.
 */
@lombok.Data
@lombok.Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
public class OrderServedEvent implements OrderScopedEvent {
    @NotNull(message = "orderId is required")
    private UUID orderId;
    @NotNull(message = "restaurantId is required")
    private UUID restaurantId;
    /** When the last line was served, on the producer's clock (consumers store this, never their receive time). */
    @NotNull(message = "servedAt is required")
    private Instant servedAt;
    /** The kitchen_staff row of the waiter who served the last line. */
    @NotNull(message = "servedByStaffId is required")
    private UUID servedByStaffId;
    /** How the order is settled: which account a booking's credit is redeemed to (Phase 6 decision 6.8). */
    @NotNull(message = "settlement is required")
    private com.fooddelivery.common.enums.DineInSettlement settlement;

    /** {@inheritDoc} */
    @Override
    public UUID orderUuid() {
        return orderId;
    }
}
