package com.fooddelivery.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

/**
 * order-events / ORDER_AT_DROP: the rider tapped "I've arrived" at the customer's door (DeliveryStatus.AT_DROP), written by
 * AtDropStateStrategy in the same transaction as the assignment's at_drop_at (DelayPenalties Phase 2).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
public class DriverAtDropEvent implements OrderScopedEvent {
    @NotNull
    private String orderId;
    @NotNull
    private String driverId;

    /**
     * When this happened, on the producer's clock, stamped in the transaction that wrote the outbox row. Consumers store
     * this value and never their own receive time, so Kafka lag, a retry back-off or a DLT replay cannot move a milestone
     * (RandomDocuments/DelayPenalties_2026-10-25, Phase 1).
     */
    @NotNull(message = "occurredAt is required")
    private java.time.Instant occurredAt;

    /**
     * Whether the delivery service's live rider position for this order was fresh (at most 30 s old) and within the
     * verification radius of the delivery pin when the tap was accepted (DelayPenalties Phase 2, D10). An unverified tap is still
     * a recorded step -- it is never refused -- but the arrival-based rules treat it as not measurable.
     */
    private boolean verified;
    /** Metres from that position to the target, rounded; null when there was no fresh position for this order (D16). */
    private Integer distanceMetres;
    /**
     * {@inheritDoc}
     *
     * <p>The wire field is a String; consumers want the UUID.
     */
    @Override
    public java.util.UUID orderUuid() {
        return orderId == null || orderId.isBlank() ? null : java.util.UUID.fromString(orderId);
    }
}
