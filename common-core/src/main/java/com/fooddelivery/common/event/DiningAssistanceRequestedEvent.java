package com.fooddelivery.common.event;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

/**
 * A seated guest asked for a waiter (dining-events, aggregate DINING_SESSION, key = sessionId; DiningPickupBooking D27).
 *
 * <p>Producer: RestaurantApplication {@code restaurant.dining} {@code AssistanceService} (Phase 3). Consumers: none
 * reads it for the floor, which reads its own table; it is published for notifications and metrics, and gains a
 * listener only in the phase that builds one. {@code reason} is the dining module's vocabulary (WATER, CUTLERY, BILL,
 * HELP), kept a String as {@link DiningSessionChangedEvent#getStatus()} is.
 */
@lombok.Data
@lombok.Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
public class DiningAssistanceRequestedEvent {
    @NotNull(message = "requestId is required")
    private UUID requestId;
    @NotNull(message = "sessionId is required")
    private UUID sessionId;
    @NotNull(message = "outletId is required")
    private UUID outletId;
    @NotNull(message = "tableId is required")
    private UUID tableId;
    @NotNull(message = "guestUserId is required")
    private UUID guestUserId;
    @NotBlank(message = "reason is required")
    private String reason;
    @NotNull(message = "requestedAt is required")
    private Instant requestedAt;
}
