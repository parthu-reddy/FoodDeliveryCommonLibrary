package com.fooddelivery.common.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/** The platform mapper must parse JSON floats as BigDecimal; money depends on it. */
class JacksonBigDecimalConfigTest {

    @Test
    void platformMapperParsesFloatsAsBigDecimalWithoutLosingDigits() throws Exception {
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        new JacksonConfig().jsonCustomizer().customize(builder);
        ObjectMapper mapper = builder.build();

        assertThat(mapper.isEnabled(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)).isTrue();

        String exact = "12345678901234567.89";
        assertThat(mapper.readTree("{\"a\":" + exact + "}").path("a").decimalValue())
                .usingComparator(BigDecimal::compareTo)
                .as("a double would render this as 12345678901234568")
                .isEqualTo(new BigDecimal(exact));
    }
}
