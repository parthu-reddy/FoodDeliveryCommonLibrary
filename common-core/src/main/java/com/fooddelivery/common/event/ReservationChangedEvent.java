package com.fooddelivery.common.event;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.Instant;
import java.util.UUID;

/**
 * A table booking was held, confirmed, modified, seated, cancelled, marked no-show, expired or completed: the booking's
 * full snapshot (reservation-events, aggregate RESERVATION, key = reservationId; DiningPickupBooking ARCHITECTURE
 * section 5).
 *
 * <p>Producer: reservation-service, in the booking write's own transaction (Phase 5). Consumers: the dining floor's
 * {@code dining_table_bookings} projection (Phase 7) and communication. A consumer applies only a newer
 * {@code version}, so reordering by non-blocking retries is harmless (CM LaterEventProvesEarlier). {@code status} is
 * reservation-service's vocabulary, kept a String as {@link DiningSessionChangedEvent#getStatus()} is.
 * {@code sessionId} is null until the party is seated; {@code guestName} is shown to staff only and never relayed by
 * SMS (D7, D46).
 */
@lombok.Data
@lombok.Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
public class ReservationChangedEvent {
    @NotNull(message = "reservationId is required")
    private UUID reservationId;
    @NotNull(message = "outletId is required")
    private UUID outletId;
    @NotNull(message = "customerUserId is required")
    private UUID customerUserId;
    @NotNull(message = "tableId is required")
    private UUID tableId;
    @Positive(message = "party must be positive")
    private int party;
    @NotNull(message = "startsAt is required")
    private Instant startsAt;
    /** The end of the table's interval {@code [startsAt, blocksUntil)}: turn time plus buffer (D30). */
    @NotNull(message = "blocksUntil is required")
    private Instant blocksUntil;
    @NotBlank(message = "status is required")
    private String status;
    private UUID sessionId;
    private String guestName;
    @PositiveOrZero(message = "version must not be negative")
    private long version;
    @NotNull(message = "occurredAt is required")
    private Instant occurredAt;
}
