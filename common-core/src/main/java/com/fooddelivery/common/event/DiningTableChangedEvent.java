package com.fooddelivery.common.event;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.Instant;
import java.util.UUID;

/**
 * A dine-in table was created, edited, deactivated or reactivated: the table's full snapshot (dining-events, aggregate
 * DINING_TABLE, key = tableId; DiningPickupBooking ARCHITECTURE section 5).
 *
 * <p>Producer: RestaurantApplication {@code restaurant.dining}, in the table write's own transaction (Phase 3).
 * Consumers: reservation-service's {@code outlet_tables} projection (Phase 5), which applies only a newer
 * {@code version}, so a reordered retry is harmless. {@code seats} is null when the outlet has not set it, as in
 * {@code dining_tables.seats}; such a table cannot be allocated to a booking.
 */
@lombok.Data
@lombok.Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
public class DiningTableChangedEvent {
    @NotNull(message = "tableId is required")
    private UUID tableId;
    @NotNull(message = "outletId is required")
    private UUID outletId;
    @NotBlank(message = "label is required")
    private String label;
    @Positive(message = "seats must be positive")
    private Integer seats;
    private boolean active;
    /** The outlet allows this table to be booked (D30 allocates only bookable tables). */
    private boolean bookable;
    @PositiveOrZero(message = "version must not be negative")
    private long version;
    @NotNull(message = "occurredAt is required")
    private Instant occurredAt;
}
