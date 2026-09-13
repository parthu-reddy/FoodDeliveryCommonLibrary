package com.fooddelivery.common.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

/**
 * Reconciles the two event shapes that coexist on several Kafka topics in this system.
 *
 * <p>Some producers publish a flat DTO ({@code OrderActionService.emitEarningsGeneratedEvent},
 * {@code CampaignServiceImpl.publishOutboxEvent}, {@code WebhookProcessingService}).
 * 
 * <p>Use these helpers rather than reading {@code eventType} directly.
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
     * <p>This is the inverse of {@link KafkaHeaderUtils#extractEventType}, which prefers the header.
     * The precedence difference is deliberate and safe, but only because of an invariant enforced
     * outside this class: {@link #conflictingBodyEventType} rejects, at the single outbox publish
     * path, any event whose payload {@code eventType} disagrees with its outbox row. Given that,
     * the two resolvers cannot return different answers for any event this platform emits.
     *
     * <p><strong>Do not treat the ordering itself as the safety property.</strong> It was once
     * used that way: {@code AdminOrderManualController} published a body {@code eventType} of
     * {@code REVERSAL_GENERATED} (a debit) under an outbox row type of {@code REFUND_GENERATED}
     * (a credit), and body-first happened to make the correct value win. That only held while every
     * consumer picked this helper rather than the header-first one -- a control that depends on
     * each caller choosing correctly is not a control. The contradiction is now rejected at the
     * source instead. See ADR 002.
     *
     * @return the resolved event type, or null when neither source carries one
     */
    public static String resolveEventType(final JsonNode root, final Map<String, Object> headers) {
        if (root != null && root.hasNonNull("eventType")) {
            return root.get("eventType").asText();
        }
        return KafkaHeaderUtils.extractHeaderValue(headers, "eventType");
    }

    /**
     * Enforces the platform's one rule about where an event type may live: the outbox row always
     * carries it, and a payload may repeat it only if it says the same thing.
     *
     * <p>Repeating it is the house convention -- 28 production call sites and 6 message contracts
     * put {@code eventType} in the body as well as leaving it on the row, and {@code OutboxProcessor}
     * turns the row's copy into the Kafka header. The duplication is harmless while the two agree.
     * A disagreement is not: the platform's two resolvers have opposite precedence, so a
     * contradictory event means the direction of a wallet movement depends on which helper the
     * consumer happens to call.
     *
     * <p>Checked at the one place every outbox event passes through, so it holds for every producer
     * without an exception list. Non-JSON and non-object payloads have nothing to contradict and
     * are passed through untouched -- this method's job is to catch contradictions, not to police
     * payload format.
     *
     * @param payloadJson  the outbox row's serialised payload
     * @param rowEventType the outbox row's event type, which becomes the Kafka header
     * @return the payload's contradicting {@code eventType}, or null when there is no contradiction
     */
    public static String conflictingBodyEventType(final String payloadJson, final String rowEventType) {
        if (payloadJson == null || rowEventType == null) {
            return null;
        }
        final JsonNode root;
        try {
            root = MAPPER.readTree(payloadJson);
        } catch (Exception e) {
            return null;
        }
        if (root == null || !root.isObject() || !root.hasNonNull("eventType")) {
            return null;
        }
        final String bodyEventType = root.get("eventType").asText();
        return rowEventType.equals(bodyEventType) ? null : bodyEventType;
    }

    /** Accepts campaignId (canonical) or id (legacy flat Campaign entity). */
    public static String campaignId(final JsonNode payload) {
        if (payload == null) return null;
        if (payload.hasNonNull("campaignId")) return payload.get("campaignId").asText();
        if (payload.hasNonNull("id"))         return payload.get("id").asText();
        return null;
    }
}
