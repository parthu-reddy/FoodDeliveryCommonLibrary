package com.fooddelivery.common.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EventPayloadUtilsTest {

    private final ObjectMapper mapper = new ObjectMapper();

    private JsonNode json(String s) throws Exception {
        return mapper.readTree(s);
    }

    @Test
    void unwrapsAnEnvelope() throws Exception {
        JsonNode p = EventPayloadUtils.unwrapPayload(
                json("{\"eventType\":\"X\",\"payload\":{\"entityId\":\"abc\"}}"));
        assertEquals("abc", p.get("entityId").asText());
    }

    @Test
    void returnsRootWhenPayloadIsFlat() throws Exception {
        // OrderActionService.emitEarningsGeneratedEvent and CampaignServiceImpl publish this shape.
        JsonNode p = EventPayloadUtils.unwrapPayload(json("{\"entityId\":\"abc\",\"amount\":\"1.50\"}"));
        assertEquals("abc", p.get("entityId").asText());
    }

    @Test
    void parsesADoubleEncodedPayloadString() throws Exception {
        JsonNode p = EventPayloadUtils.unwrapPayload(
                json("{\"eventType\":\"X\",\"payload\":\"{\\\"entityId\\\":\\\"abc\\\"}\"}"));
        assertEquals("abc", p.get("entityId").asText());
    }

    @Test
    void treatsNullPayloadAsFlat() throws Exception {
        JsonNode p = EventPayloadUtils.unwrapPayload(json("{\"entityId\":\"abc\",\"payload\":null}"));
        assertEquals("abc", p.get("entityId").asText());
    }

    @Test
    void resolvesEventTypeFromBody() throws Exception {
        assertEquals("FROM_BODY",
                EventPayloadUtils.resolveEventType(json("{\"eventType\":\"FROM_BODY\"}"), Map.of()));
    }

    @Test
    void fallsBackToHeaderWhenBodyHasNone() throws Exception {
        // The flat producers carry no body eventType; OutboxProcessor supplies it as a header.
        assertEquals("FROM_HEADER",
                EventPayloadUtils.resolveEventType(json("{\"entityId\":\"abc\"}"),
                        Map.of("eventType", "FROM_HEADER")));
    }

    @Test
    void bodyWinsWhenBodyAndHeaderDisagree() throws Exception {
        // THE critical case. AdminOrderManualController sends body REVERSAL_GENERATED (a debit)
        // under outbox eventType REFUND_GENERATED (a credit). Preferring the header would turn a
        // refund reversal into a credit. If this assertion is ever flipped, money moves the wrong way.
        assertEquals("REVERSAL_GENERATED",
                EventPayloadUtils.resolveEventType(
                        json("{\"eventType\":\"REVERSAL_GENERATED\",\"payload\":{}}"),
                        Map.of("eventType", "REFUND_GENERATED")));
    }

    @Test
    void returnsNullWhenNeitherSourceHasIt() throws Exception {
        assertNull(EventPayloadUtils.resolveEventType(json("{\"entityId\":\"abc\"}"), Map.of()));
    }

    @Test
    void decodesByteArrayHeaders() throws Exception {
        // Kafka headers arrive as byte[]; OutboxProcessor writes UTF-8 bytes.
        assertEquals("FROM_HEADER",
                EventPayloadUtils.resolveEventType(json("{}"),
                        Map.of("eventType", "FROM_HEADER".getBytes(java.nio.charset.StandardCharsets.UTF_8))));
    }

    @Test
    void handlesNullRoot() {
        assertNull(EventPayloadUtils.unwrapPayload(null));
        assertTrue(EventPayloadUtils.resolveEventType(null, Map.of()) == null);
    }
}
