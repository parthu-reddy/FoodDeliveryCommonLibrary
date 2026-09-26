package com.fooddelivery.common.time;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * The platform's one clock.
 *
 * <p>UTC, so that nothing reading it depends on the JVM's zone. Calendar questions ("which day is
 * it for this outlet?") take their zone from data or configuration and pass it explicitly to
 * {@link BusinessCalendar}. They never come from the clock. Two services used to define their own
 * clock zoned to a platform-wide business zone, and that is how a UTC wall-clock reading came to be
 * interpreted as Asia/Kolkata (the review window closed 5h30 early).
 * RandomDocuments/TimezoneCorrectness_2026-09-25.
 *
 * <p>Inject {@link Clock} wherever "now" should be controllable in a test.
 */
@Configuration
public class TimeConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
