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
