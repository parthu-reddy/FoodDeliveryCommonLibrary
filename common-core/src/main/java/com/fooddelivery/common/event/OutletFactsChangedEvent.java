package com.fooddelivery.common.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.Instant;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

/**
 * What another service needs to know about an outlet: the full snapshot of whose it is, its zone, whether it is serving and
 * when it opens (restaurant-events, aggregate OUTLET, key = outletId; DiningPickupBooking Phase 5 plan 5.5).
 *
 * <p>Producer: RestaurantApplication's {@code OutletFactsPublisher}, in the transaction of every write to one of these facts.
 * Consumer: reservation-service's {@code outlet_facts} projection, which applies only a newer {@code version}
 * ({@code outlets.facts_version}), so a reordered retry is harmless. {@code openingWindows} are wall-clock times in
 * {@code timeZone}, half-open, read by {@code OpeningHours}; an empty list means the outlet never opens.
 */
@lombok.Data
@lombok.Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
public class OutletFactsChangedEvent {
    @NotNull(message = "outletId is required")
    private UUID outletId;
    @NotNull(message = "organisationId is required")
    private UUID organisationId;
    @NotBlank(message = "timeZone is required")
    private String timeZone;
    private boolean active;
    /** The outlet's brand is APPROVED: only then may it serve. */
    private boolean brandApproved;
    @NotNull(message = "openingWindows is required")
    @Valid
    private List<OpeningWindow> openingWindows;
    @PositiveOrZero(message = "version must not be negative")
    private long version;
    @NotNull(message = "occurredAt is required")
    private Instant occurredAt;

    /** One daily opening, {@code [opens, closes)}: see {@code OpeningHours} for midnight and all-day windows. */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    public static class OpeningWindow {
        @NotNull(message = "opens is required")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
        private LocalTime opens;
        @NotNull(message = "closes is required")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
        private LocalTime closes;
    }
}
