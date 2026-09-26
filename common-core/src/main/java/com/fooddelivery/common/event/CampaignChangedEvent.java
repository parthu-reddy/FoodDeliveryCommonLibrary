package com.fooddelivery.common.event;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

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
public class CampaignChangedEvent {
    private UUID campaignId;
    private UUID advertiserId;
    private String status;
    private BigDecimal maxBid;
    private BigDecimal budget;
    private Boolean budgetExhausted;
    private Double pacingMultiplier;
    private Integer schemaVersion;
    private com.fooddelivery.common.dto.targeting.TargetingSummary targeting;
    private String creativeFormat;
    private String creativeAssetUrl;
    private String creativeVastXml;
    /** The advertiser's IANA zone: dayparting, daily budgets and reporting days are on its calendar. */
    private String timeZone;
}
