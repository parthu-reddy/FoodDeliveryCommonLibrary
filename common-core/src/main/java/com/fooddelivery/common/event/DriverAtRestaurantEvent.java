package com.fooddelivery.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
/**
 * Wire shape tolerates fields it does not declare -- {@code eventType} above all.
 *
 * <p>Repeating eventType in the body is this platform's convention: EventPayloadUtils records 28
 * production call sites and 6 message contracts doing it, WebhookProcessingService adds it with
 * {@code valueToTree(event).put("eventType", ...)}, and OutboxProcessor enforces only that it must
 * not CONTRADICT the outbox row (ADR 002). So the class genuinely receives a field it has no
 * component for.
 *
 * <p>Declared here rather than left to the ObjectMapper's global setting. Boot's mapper disables
 * FAIL_ON_UNKNOWN_PROPERTIES, but a plain {@code new ObjectMapper()} does not, and several test
 * harnesses build one -- PaymentEventConsumerRefundIdempotencyTest threw
 * UnrecognizedPropertyException on exactly this field. Binding must not depend on which mapper
 * happens to be wired.
 */
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
public class DriverAtRestaurantEvent implements OrderScopedEvent {
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
     * verification radius of the outlet when the tap was accepted (DelayPenalties Phase 2, D10). An unverified tap is still
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
