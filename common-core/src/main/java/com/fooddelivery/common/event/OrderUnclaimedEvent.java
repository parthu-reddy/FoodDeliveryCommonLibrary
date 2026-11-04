package com.fooddelivery.common.event;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

/**
 * A ready PICKUP order was never collected and is now closed (order-events, aggregate ORDER, key = orderId;
 * DiningPickupBooking D17).
 *
 * <p>Producer: OrderService's {@code UnclaimedPickupSweeper}, at READY plus the outlet's unclaimed window (Phase 2).
 * Consumers: RestaurantApplication restaurant core closes the order and the kitchen closes the ticket. It is booked like
 * a collection (the food was made), with no automatic refund.
 */
@lombok.Data
@lombok.Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
public class OrderUnclaimedEvent implements OrderScopedEvent {
    @NotNull(message = "orderId is required")
    private UUID orderId;
    @NotNull(message = "restaurantId is required")
    private UUID restaurantId;
    /** When the sweeper closed it, on order-service's clock. */
    @NotNull(message = "unclaimedAt is required")
    private Instant unclaimedAt;

    /** {@inheritDoc} */
    @Override
    public UUID orderUuid() {
        return orderId;
    }
}
