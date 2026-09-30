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
public class ForceAssignDriverEvent implements OrderScopedEvent {
    @NotNull(message = "orderId is required")
    private String orderId;
    private String customerId;
    @NotNull(message = "driverId is required")
    private String driverId;
    /**
     * Stable identifier for one audited admin operation. Delivery uses it to reject a stale
     * command when a newer intervention superseded it before Kafka delivery.
     */
    private String operationId;
    /** Authenticated admin identity recorded by CustomerApplication. */
    private String actorId;
    /** Human explanation required for a manual override. */
    private String reason;
    /** The order's authoritative dispatch city at the moment the operation was requested. */
    private String dispatchCityId;
    private Long timestamp;


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
