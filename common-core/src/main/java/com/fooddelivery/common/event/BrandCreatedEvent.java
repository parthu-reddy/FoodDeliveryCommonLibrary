package com.fooddelivery.common.event;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotNull;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
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
public class BrandCreatedEvent {
    @NotNull private String brandId;
    @NotNull private String brandName;
    @NotNull private String gstin;
    @NotNull private String bankAccountNumber;
    @NotNull private String ifscCode;
}
