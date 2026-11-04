package com.fooddelivery.common.event;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

/**
 * A PICKUP order was handed over at the counter (order-events, aggregate ORDER, key = orderId; DiningPickupBooking D15).
 *
 * <p>Producer: RestaurantApplication restaurant core, from the counter handover (Phase 2). Consumers: OrderService
 * completes the order and books it like a delivery minus the rider and delivery legs ({@code bookCollected}); the
 * kitchen closes the ticket. {@code restaurantId} is the outlet, as on every other order event.
 */
@lombok.Data
@lombok.Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
public class OrderCollectedEvent implements OrderScopedEvent {
    @NotNull(message = "orderId is required")
    private UUID orderId;
    @NotNull(message = "restaurantId is required")
    private UUID restaurantId;
    /** When the food was handed over, on the producer's clock (consumers store this, never their receive time). */
    @NotNull(message = "collectedAt is required")
    private Instant collectedAt;
    /**
     * The user who handed it over: a waiter, the kitchen MANAGER or an organisation member (DiningCapability.HANDOVER_PICKUP).
     * A user id, not a kitchen_staff id: an organisation member has no staff row.
     */
    @NotNull(message = "handedOverByUserId is required")
    private UUID handedOverByUserId;
    /** A kitchen MANAGER handed over without the customer's code after checking name and phone (D14). */
    private boolean withoutCode;

    /** {@inheritDoc} */
    @Override
    public UUID orderUuid() {
        return orderId;
    }
}
