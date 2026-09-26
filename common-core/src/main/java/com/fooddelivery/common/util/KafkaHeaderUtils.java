package com.fooddelivery.common.util;

import com.fasterxml.jackson.databind.JsonNode;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Utility class for extracting Kafka header values, handling the
 * NonTrustedHeaderType deserialization edge case common in Spring Kafka.
 */
public final class KafkaHeaderUtils {

    private KafkaHeaderUtils() {}

    // Spring Kafka's KafkaHeaders.RECEIVED_TOPIC, RECEIVED_PARTITION and OFFSET. Spelled out because
    // common-core does not depend on spring-kafka; DeadLetterPositionTest in common-messaging pins them.
    static final String RECEIVED_TOPIC = "kafka_receivedTopic";
    static final String RECEIVED_PARTITION = "kafka_receivedPartitionId";
    static final String OFFSET = "kafka_offset";

    /**
     * Where a dead-letter record sits, as the exact JSON body the admin DLQ retry endpoints take
     * ({@code POST /api/v1/internal/admin/{wallet|payments|orders}/dlq/retry}). Every {@code @DltHandler}
     * logs it, so a dead-lettered record can be replayed from its log line alone.
     *
     * @param headers the {@code @Headers} map of a record consumed from a dead-letter topic
     */
    public static String deadLetterPosition(Map<String, Object> headers) {
        return position(String.valueOf(headers.get(RECEIVED_TOPIC)), String.valueOf(headers.get(RECEIVED_PARTITION)),
                String.valueOf(headers.get(OFFSET)));
    }

    /** The same body, for a dead-letter record known by where it was written. */
    public static String deadLetterPosition(String dltTopic, int partition, long offset) {
        return position(dltTopic, String.valueOf(partition), String.valueOf(offset));
    }

    private static String position(String dltTopic, String partition, String offset) {
        // Kafka topic names are [a-zA-Z0-9._-], so the name needs no JSON escaping.
        return "{\"dltTopic\":\"" + dltTopic + "\",\"partition\":" + partition + ",\"offset\":" + offset + "}";
    }

    /**
     * Extracts the eventType from Kafka headers, falling back to the JSON payload.
     * Handles byte[], NonTrustedHeaderType wrappers, and plain String objects.
     *
     * @param headers     The Kafka message headers map
     * @param jsonPayload The parsed JSON payload (used as fallback)
     * @return The resolved event type string, or null if not found
     */
    public static String extractEventType(Map<String, Object> headers, JsonNode jsonPayload) {
        String headerEventType = extractHeaderValue(headers, "eventType");
        String jsonEventType = (jsonPayload != null) ? jsonPayload.path("eventType").asText(null) : null;
        return headerEventType != null ? headerEventType : jsonEventType;
    }

    /**
     * Extracts a string value from Kafka headers, handling NonTrustedHeaderType wrappers.
     */
    public static String extractHeaderValue(Map<String, Object> headers, String headerName) {
        if (headers == null) return null;
        
        Object value = headers.get(headerName);
        if (value == null) return null;

        if (value instanceof byte[]) {
            return new String((byte[]) value, StandardCharsets.UTF_8);
        }
        
        if (value.getClass().getName().contains("NonTrustedHeaderType")) {
            String str = value.toString();
            if (str.contains("headerValue=")) {
                int start = str.indexOf("\"") + 1;
                if (start > 0) {
                    int end = str.indexOf("\"", start);
                    if (end > start) {
                        return str.substring(start, end);
                    }
                }
            }
            return str;
        }
        
        return value.toString();
    }
}
