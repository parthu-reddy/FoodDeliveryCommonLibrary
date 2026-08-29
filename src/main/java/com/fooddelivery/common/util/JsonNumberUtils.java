package com.fooddelivery.common.util;

/**
 * Reads numbers out of loosely-typed JSON payloads (Feign {@code Map<String, Object>} responses,
 * {@code ObjectMapper.convertValue} output).
 *
 * <p>Casting such a value directly to a concrete box type is never safe. JacksonConfig enables
 * {@code USE_BIG_DECIMAL_FOR_FLOATS} fleet-wide, so a JSON float deserialises to
 * {@link java.math.BigDecimal} and {@code (Double) map.get("lat")} throws ClassCastException on
 * every request. Integral JSON values arrive as Integer or Long regardless of that setting, so the
 * cast is wrong even with the feature off.
 *
 * <p>Go through {@link Number} instead — BigDecimal, Integer, Long and Double all implement it.
 */
public final class JsonNumberUtils {

    private JsonNumberUtils() {
    }

    /**
     * @return the value as a Double, or null when absent or not numeric.
     */
    public static Double toDouble(Object value) {
        return value instanceof Number ? ((Number) value).doubleValue() : null;
    }
}
