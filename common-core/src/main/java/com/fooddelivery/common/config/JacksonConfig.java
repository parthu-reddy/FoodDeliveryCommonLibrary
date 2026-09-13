package com.fooddelivery.common.config;

import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
        return builder -> {
            // Serialize LocalDateTime with 'Z' appended to satisfy ISO-8601 strict datetime formatting
            DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                    .appendPattern("yyyy-MM-dd'T'HH:mm:ss")
                    .optionalStart()
                    .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
                    .optionalEnd()
                    .appendLiteral('Z')
                    .toFormatter();
            builder.serializers(new LocalDateTimeSerializer(formatter));
            
            // Lenient deserializer that can parse both with and without 'Z'
            DateTimeFormatter parserFormatter = new DateTimeFormatterBuilder()
                    .appendPattern("yyyy-MM-dd'T'HH:mm:ss")
                    .optionalStart()
                    .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
                    .optionalEnd()
                    .optionalStart()
                    .appendLiteral('Z')
                    .optionalEnd()
                    .toFormatter();
            builder.deserializers(new com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer(parserFormatter));

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
