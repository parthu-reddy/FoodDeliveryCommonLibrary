package com.fooddelivery.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Pins the runtime types a Feign {@code Map<String, Object>} actually carries under the platform
 * mapper, and that {@link JsonNumberUtils#toDouble} survives all of them.
 *
 * <p>Guards the 2026-08-29 outage: {@code (Double) restaurantData.get("lat")} threw
 * ClassCastException on every request once USE_BIG_DECIMAL_FOR_FLOATS was enabled fleet-wide.
 * JacksonBigDecimalConfigTest did not catch it because it only exercised the JsonNode API.
 */
class JsonNumberUtilsTest {

    private Map<String, Object> parse(String json) throws Exception {
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        new com.fooddelivery.common.config.JacksonConfig().jsonCustomizer().customize(builder);
        ObjectMapper mapper = builder.build();
        return mapper.readValue(json, Map.class);
    }

    @Test
    void decimalJsonValuesArriveAsBigDecimalAndBreakADirectDoubleCast() throws Exception {
        Object lat = parse("{\"lat\":12.9716}").get("lat");

        assertThat(lat).isInstanceOf(BigDecimal.class);
        assertThatThrownBy(() -> { Double ignored = (Double) lat; })
                .as("the exact shape of the outage")
                .isInstanceOf(ClassCastException.class);
    }

    @Test
    void integralJsonValuesArriveAsIntegerAndAlsoBreakADirectDoubleCast() throws Exception {
        Object whole = parse("{\"lat\":13}").get("lat");

        assertThat(whole).isInstanceOf(Integer.class);
        assertThatThrownBy(() -> { Double ignored = (Double) whole; })
                .as("the cast was wrong even before USE_BIG_DECIMAL_FOR_FLOATS")
                .isInstanceOf(ClassCastException.class);
    }

    @Test
    void toDoubleHandlesEveryShapeAFeignMapCanCarry() throws Exception {
        Map<String, Object> m = parse("{\"dec\":12.9716,\"whole\":13,\"big\":9007199254740993,\"nil\":null}");

        assertThat(JsonNumberUtils.toDouble(m.get("dec"))).isEqualTo(12.9716);
        assertThat(JsonNumberUtils.toDouble(m.get("whole"))).isEqualTo(13.0);
        assertThat(JsonNumberUtils.toDouble(m.get("big"))).isNotNull();
        assertThat(JsonNumberUtils.toDouble(m.get("nil"))).isNull();
        assertThat(JsonNumberUtils.toDouble(m.get("absent"))).isNull();
    }

    @Test
    void toDoubleReturnsNullForNonNumericRatherThanThrowing() {
        assertDoesNotThrow(() -> JsonNumberUtils.toDouble("12.9716"));
        assertThat(JsonNumberUtils.toDouble("12.9716")).as("a String coordinate is bad data, not a Double").isNull();
        assertThat(JsonNumberUtils.toDouble(Boolean.TRUE)).isNull();
    }
}
