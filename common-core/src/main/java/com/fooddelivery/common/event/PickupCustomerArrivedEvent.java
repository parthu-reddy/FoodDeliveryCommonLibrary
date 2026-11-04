package com.fooddelivery.common.event;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

/**
 * The customer tapped "I'm here" for a PICKUP order (order-events, aggregate ORDER, key = orderId; DiningPickupBooking
 * D16).
 *
 * <p>Producer: OrderService, from the customer's arrived call (Phase 2). Consumers: the kitchen flags the counter card
 * "Customer waiting" from {@code arrivedAt}. A repeat tap publishes again with a later {@code arrivedAt}.
 */
@lombok.Data
@lombok.Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
public class PickupCustomerArrivedEvent implements OrderScopedEvent {
    @NotNull(message = "orderId is required")
    private UUID orderId;
    @NotNull(message = "restaurantId is required")
    private UUID restaurantId;
    @NotNull(message = "arrivedAt is required")
    private Instant arrivedAt;

    /** {@inheritDoc} */
    @Override
    public UUID orderUuid() {
        return orderId;
    }
}
