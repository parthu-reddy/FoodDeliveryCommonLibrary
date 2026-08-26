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
        };
    }
}
