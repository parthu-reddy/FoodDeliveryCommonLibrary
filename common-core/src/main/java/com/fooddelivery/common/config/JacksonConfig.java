package com.fooddelivery.common.config;

import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
        return builder -> {
            // Every moment on the wire is an Instant, written as ISO-8601 UTC ("...Z") and read
            // strictly: a timestamp with no offset is rejected rather than guessed at.
            //
            // This mapper used to serialize LocalDateTime with a literal 'Z' appended, and read
            // "...Z" / "...+05:30" back into a LocalDateTime by dropping the offset. The 'Z' was
            // true only while the JVM ran in UTC: measured 2026-09-25, the same 10:00 IST instant
            // went out as 10:00Z (+5h30 wrong) from an IST JVM and as 21:30Z the previous day from
            // a Los Angeles one. RandomDocuments/TimezoneCorrectness_2026-09-25.
            //
            // Pinning the mapper's zone matters only for zoned types, which the platform no longer
            // stores or sends; without it an OffsetDateTime is written in the JVM's offset.
            builder.timeZone("UTC");

            // Parse JSON floating-point numbers as BigDecimal rather than double.
            //
            // Without this, readTree() produces a DoubleNode and the value is already through
            // binary floating point before any code touches it: 12345678901234567.89 arrives as
            // 12345678901234568. No call-site fix can recover that -- BigDecimal.valueOf(asDouble())
            // and new BigDecimal(asText()) are byte-identical on a DoubleNode, which is why the
            // apparent fix at PaymentEventConsumer changed nothing (measured 2026-08-27).
            //
            // Safe here: nothing in the fleet branches on node type (zero isDouble() or
            // `instanceof DoubleNode`), and the .doubleValue() call sites all go through Number,
            // which BigDecimal implements. Note Jackson still strips trailing zeros, so 15.50
            // reads back as 15.5 -- the VALUE is exact, the scale is not a JSON property.
            builder.featuresToEnable(
                    com.fasterxml.jackson.databind.DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
        };
    }
}
