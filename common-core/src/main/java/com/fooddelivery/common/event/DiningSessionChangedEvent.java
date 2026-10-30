package com.fooddelivery.common.event;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.Instant;
import java.util.UUID;

/**
 * A dine-in table session opened, changed waiter or closed (dining-events, aggregate DINING_SESSION, key = sessionId).
 *
 * <p>For live floor updates and a future dining-service. {@code status} is the dining module's session status, kept a
 * String so the module owns its own vocabulary. {@code waiterStaffId} is null while the session sits in the outlet pool.
 * {@code version} is monotonic per session.
 */
@lombok.Data
@lombok.Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
public class DiningSessionChangedEvent {
    @NotNull(message = "sessionId is required")
    private UUID sessionId;
    @NotNull(message = "outletId is required")
    private UUID outletId;
    @NotNull(message = "tableId is required")
    private UUID tableId;
    @NotBlank(message = "status is required")
    private String status;
    private UUID waiterStaffId;
    @PositiveOrZero(message = "version must not be negative")
    private long version;
    @NotNull(message = "occurredAt is required")
    private Instant occurredAt;
}
