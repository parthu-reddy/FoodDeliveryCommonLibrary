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
public class OrderStatusSyncEvent implements OrderScopedEvent {
    @NotNull(message = "orderId is required")
    private String orderId;
    private String status;


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
