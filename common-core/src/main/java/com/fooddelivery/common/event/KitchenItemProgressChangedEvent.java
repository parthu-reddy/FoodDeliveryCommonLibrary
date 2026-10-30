package com.fooddelivery.common.event;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * One order's kitchen lines after a ready, status or void change (kitchen-events, aggregate KITCHEN_ITEM, key = orderId).
 *
 * <p>State-carrying: every line of the ticket, not the delta, so a consumer that missed an event still converges.
 * {@code version} is monotonic per order; a consumer applies an event only if it is newer than what it holds, so a late
 * or replayed event cannot undo a newer one. Published for every ticket; the dining module is its only consumer.
 */
@lombok.Data
@lombok.Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
public class KitchenItemProgressChangedEvent implements OrderScopedEvent {
    @NotNull(message = "orderId is required")
    private UUID orderId;
    @NotNull(message = "outletId is required")
    private UUID outletId;
    @PositiveOrZero(message = "version must not be negative")
    private long version;
    @NotEmpty(message = "lines are required")
    @Valid
    private List<Line> lines;
    @NotNull(message = "occurredAt is required")
    private Instant occurredAt;

    /** One order line in the kitchen: {@code readyQuantity + voidQuantity <= quantity}. */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    public static class Line {
        @NotNull(message = "orderItemId is required")
        private UUID orderItemId;
        @Positive(message = "quantity must be positive")
        private int quantity;
        @PositiveOrZero(message = "readyQuantity must not be negative")
        private int readyQuantity;
        @PositiveOrZero(message = "voidQuantity must not be negative")
        private int voidQuantity;

        @jakarta.validation.constraints.AssertTrue(message = "readyQuantity + voidQuantity must not exceed quantity")
        @com.fasterxml.jackson.annotation.JsonIgnore
        public boolean isWithinQuantity() {
            return readyQuantity + voidQuantity <= quantity;
        }
    }

    /** {@inheritDoc} */
    @Override
    public UUID orderUuid() {
        return orderId;
    }
}
