package com.fooddelivery.common.event;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.Instant;
import java.util.UUID;

/**
 * An outlet's service modes changed: the full {@code outlet_service_modes} row (restaurant-events, aggregate OUTLET,
 * key = outletId; DiningPickupBooking D4). Each mode's field lands with its column: pickup in Phase 2, self-order in
 * Phase 3, reservations in Phase 5 (DiningPickupBooking Phase 2 plan, decided at its start).
 *
 * <p>Producer: RestaurantApplication, from the service-modes write. Consumers land with their phase: order-service reads
 * the pickup modes from the outlet facts it already fetches at checkout, so nothing consumes this event in Phase 2. A
 * consumer applies only a newer {@code version}. Each mode has one predicate, which both the UI entry point and the
 * server action use.
 */
@lombok.Data
@lombok.Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
public class OutletServiceModesChangedEvent {
    @NotNull(message = "outletId is required")
    private UUID outletId;
    private boolean pickupEnabled;
    /** Minutes after READY at which an uncollected pickup closes as unclaimed (D17). */
    @Min(value = 60, message = "pickupUnclaimedMinutes must be at least 60")
    @Max(value = 240, message = "pickupUnclaimedMinutes must be at most 240")
    private int pickupUnclaimedMinutes;
    /** Guests may seat themselves and order from their own phones (D21). */
    private boolean selfOrderEnabled;
    /** A self-ordering guest may pay at the restaurant once staff have seen the table (D26). */
    private boolean selfOrderPayAtRestaurant;
    /** How close to the outlet a device must be for its location to prove presence (D22). */
    @Min(value = 50, message = "geofenceMetres must be at least 50")
    @Max(value = 500, message = "geofenceMetres must be at most 500")
    private int geofenceMetres;
    @PositiveOrZero(message = "version must not be negative")
    private long version;
    @NotNull(message = "occurredAt is required")
    private Instant occurredAt;
}
