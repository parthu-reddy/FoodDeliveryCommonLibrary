package com.fooddelivery.common.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

/**
 * Reconciles the two event shapes that coexist on several Kafka topics in this system.
 *
 * <p>Some producers publish a flat DTO ({@code OrderActionService.emitEarningsGeneratedEvent},
 * {@code CampaignServiceImpl.publishOutboxEvent}, {@code WebhookProcessingService}); others wrap it
 * as {@code {eventType, payload}} ({@code AdminOrderManualController}). Consumers written against
 * only one shape silently drop the other -- no exception, no DLQ entry, nothing logged at error
 * level. That defect has been found six times in this codebase, twice involving money.
 *
 * <p>Use these helpers rather than reading {@code payload} or {@code eventType} directly.
 */
public final class EventPayloadUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private EventPayloadUtils() {}

    /**
     * Returns the node carrying the business fields, accepting either shape.
     *
     * <p>Handles three cases: an {@code {eventType, payload}} envelope, a flat payload where the
     * root itself carries the fields, and an envelope whose {@code payload} is a JSON string rather
     * than an object (some outbox producers double-encode).
     *
     * @return the payload node, or the root when there is no envelope; never null for non-null input
     */
    public static JsonNode unwrapPayload(final JsonNode root) {
        if (root == null) {
            return null;
        }
        final JsonNode payload = root.get("payload");
        if (payload == null || payload.isNull()) {
            return root;
        }
        if (payload.isTextual()) {
            try {
                return MAPPER.readTree(payload.asText());
            } catch (Exception e) {
                return root;
            }
        }
        return payload;
    }

    /**
     * Resolves the event type from the message BODY first, falling back to the Kafka header.
     *
     * <p><strong>The ordering is deliberate and must not be reversed.</strong> It is the inverse of
     * {@link KafkaHeaderUtils#extractEventType}, which prefers the header. On {@code wallet-events}
     * the two disagree on purpose: {@code AdminOrderManualController} publishes a body
     * {@code eventType} of {@code REVERSAL_GENERATED} (which a consumer treats as a debit) under an
     * outbox event type of {@code REFUND_GENERATED} (a credit). Preferring the header there turns a
     * refund reversal into a credit -- real money, wrong direction, and no error.
     *
     * <p>Body-first is also strictly additive: every path that already worked behaves identically,
     * and only messages that previously had no resolvable event type start being handled.
     *
     * @return the resolved event type, or null when neither source carries one
     */
    public static String resolveEventType(final JsonNode root, final Map<String, Object> headers) {
        if (root != null && root.hasNonNull("eventType")) {
            return root.get("eventType").asText();
        }
        return KafkaHeaderUtils.extractHeaderValue(headers, "eventType");
    }

    /** Accepts campaignId (canonical) or id (legacy flat Campaign entity). */
    public static String campaignId(final JsonNode payload) {
        if (payload == null) return null;
        if (payload.hasNonNull("campaignId")) return payload.get("campaignId").asText();
        if (payload.hasNonNull("id"))         return payload.get("id").asText();
        return null;
    }
}
