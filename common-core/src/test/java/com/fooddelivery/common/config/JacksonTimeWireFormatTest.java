package com.fooddelivery.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * What the platform actually puts on the wire for time values.
 *
 * <p>Built through Boot's real {@link JacksonAutoConfiguration} plus {@link JacksonConfig}, not a
 * hand-assembled mapper: the first probe of this (2026-09-25) used a bare builder, missed Boot's
 * WRITE_DATES_AS_TIMESTAMPS=false default, and printed a format production never sends.
 *
 * <p>The build runs these in a hostile JVM zone (Pacific/Chatham), so every assertion here also
 * proves the output does not follow the JVM's zone.
 */
class JacksonTimeWireFormatTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(JacksonAutoConfiguration.class))
            .withUserConfiguration(JacksonConfig.class);

    private static final Instant T = Instant.parse("2026-09-25T04:30:00Z"); // 10:00 IST

    @Test
    void instantsAreWrittenAsUtcWithZ() {
        runner.run(ctx -> {
            ObjectMapper m = ctx.getBean(ObjectMapper.class);
            assertThat(m.writeValueAsString(T)).isEqualTo("\"2026-09-25T04:30:00Z\"");
            assertThat(m.writeValueAsString(T.plusNanos(123_456_000))).isEqualTo("\"2026-09-25T04:30:00.123456Z\"");
        });
    }

    @Test
    void anOffsetOnInputIsNormalisedToTheSameInstant() {
        runner.run(ctx -> {
            ObjectMapper m = ctx.getBean(ObjectMapper.class);
            assertThat(m.readValue("\"2026-09-25T10:00:00+05:30\"", Instant.class)).isEqualTo(T);
            assertThat(m.readValue("\"2026-09-25T04:30:00Z\"", Instant.class)).isEqualTo(T);
        });
    }

    @Test
    void aZonelessTimestampIsRejectedNotGuessed() {
        runner.run(ctx -> {
            ObjectMapper m = ctx.getBean(ObjectMapper.class);
            assertThatThrownBy(() -> m.readValue("\"2026-09-25T10:00:00\"", Instant.class))
                    .isInstanceOf(InvalidFormatException.class);
        });
    }

    @Test
    void theMapperIsPinnedToUtcSoAStrayZonedValueStillComesOutInZ() {
        runner.run(ctx -> {
            ObjectMapper m = ctx.getBean(ObjectMapper.class);
            OffsetDateTime ist = T.atOffset(ZoneOffset.ofHoursMinutes(5, 30));
            assertThat(m.writeValueAsString(ist)).isEqualTo("\"2026-09-25T04:30:00Z\"");
        });
    }

    @Test
    void calendarValuesStayCalendarValues() {
        runner.run(ctx -> {
            ObjectMapper m = ctx.getBean(ObjectMapper.class);
            assertThat(m.writeValueAsString(LocalDate.of(2026, 9, 25))).isEqualTo("\"2026-09-25\"");
            assertThat(m.writeValueAsString(LocalTime.of(22, 0))).isEqualTo("\"22:00:00\"");
        });
    }

    @Test
    void theLiteralZHackIsGone() {
        // It used to write a LocalDateTime with a literal 'Z', asserting a zone the value never had.
        runner.run(ctx -> {
            ObjectMapper m = ctx.getBean(ObjectMapper.class);
            assertThat(m.writeValueAsString(LocalDateTime.of(2026, 9, 25, 10, 0))).doesNotEndWith("Z\"");
        });
    }
}
