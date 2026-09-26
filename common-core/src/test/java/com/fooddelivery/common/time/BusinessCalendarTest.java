package com.fooddelivery.common.time;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

class BusinessCalendarTest {

    private static final ZoneId KOLKATA = ZoneId.of("Asia/Kolkata");
    private static final ZoneId NEW_YORK = ZoneId.of("America/New_York");
    private static final ZoneId ST_JOHNS = ZoneId.of("America/St_Johns");
    private static final ZoneId CHATHAM = ZoneId.of("Pacific/Chatham");

    @Test
    void todayIsTheDateInTheGivenZoneNotTheJvmZone() {
        // 20:45Z is already the 26th in Kolkata (02:15) and still the 25th in St John's (18:15).
        Clock clock = Clock.fixed(Instant.parse("2026-09-25T20:45:00Z"), ZoneOffset.UTC);
        assertThat(BusinessCalendar.today(clock, KOLKATA)).isEqualTo(LocalDate.of(2026, 9, 26));
        assertThat(BusinessCalendar.today(clock, ST_JOHNS)).isEqualTo(LocalDate.of(2026, 9, 25));
        assertThat(BusinessCalendar.today(clock, CHATHAM)).isEqualTo(LocalDate.of(2026, 9, 26));
    }

    @Test
    void aDayIsHalfOpenAndStartsAtLocalMidnight() {
        TimeWindow day = BusinessCalendar.day(LocalDate.of(2026, 9, 25), KOLKATA);
        assertThat(day.from()).isEqualTo(Instant.parse("2026-09-24T18:30:00Z"));
        assertThat(day.to()).isEqualTo(Instant.parse("2026-09-25T18:30:00Z"));
        assertThat(day.contains(day.from())).isTrue();
        assertThat(day.contains(day.to())).as("the end belongs to the next day").isFalse();
    }

    @Test
    void daysFollowDaylightSaving() {
        // 2026-03-08 New York springs forward (23h); 2026-11-01 falls back (25h).
        assertThat(BusinessCalendar.day(LocalDate.of(2026, 3, 8), NEW_YORK).length()).isEqualTo(Duration.ofHours(23));
        assertThat(BusinessCalendar.day(LocalDate.of(2026, 11, 1), NEW_YORK).length()).isEqualTo(Duration.ofHours(25));
        assertThat(BusinessCalendar.day(LocalDate.of(2026, 9, 25), KOLKATA).length()).isEqualTo(Duration.ofHours(24));
    }

    @Test
    void consecutiveDaysTileWithoutGapOrOverlap() {
        LocalDate d = LocalDate.of(2026, 3, 7);
        for (int i = 0; i < 10; i++, d = d.plusDays(1)) {
            assertThat(BusinessCalendar.day(d, NEW_YORK).to()).isEqualTo(BusinessCalendar.day(d.plusDays(1), NEW_YORK).from());
        }
    }

    @Test
    void fractionOfDayUsesTheRealLengthOfThatDay() {
        // Noon on the 23h spring-forward day: 11 real hours have passed (02:00-03:00 never happened).
        Instant noonSpring = LocalDate.of(2026, 3, 8).atTime(12, 0).atZone(NEW_YORK).toInstant();
        assertThat(BusinessCalendar.fractionOfDayElapsed(noonSpring, NEW_YORK)).isCloseTo(11.0 / 23.0, within(1e-9));

        // One second before midnight on the 25h fall-back day is still short of the whole day.
        Instant lastSecond = LocalDate.of(2026, 11, 1).atTime(23, 59, 59).atZone(NEW_YORK).toInstant();
        double f = BusinessCalendar.fractionOfDayElapsed(lastSecond, NEW_YORK);
        assertThat(f).isLessThan(1.0).isCloseTo((25 * 3600 - 1) / (25.0 * 3600), within(1e-9));

        Instant midnight = BusinessCalendar.startOfDay(LocalDate.of(2026, 9, 25), CHATHAM);
        assertThat(BusinessCalendar.fractionOfDayElapsed(midnight, CHATHAM)).isZero();
    }

    @Test
    void localTimeAndDateReadTheWallClockOfTheZone() {
        Instant i = Instant.parse("2026-09-25T04:30:00Z");
        assertThat(BusinessCalendar.localTime(i, KOLKATA)).isEqualTo(LocalTime.of(10, 0));
        assertThat(BusinessCalendar.localTime(i, CHATHAM)).isEqualTo(LocalTime.of(17, 15));
        assertThat(BusinessCalendar.localDate(i, ST_JOHNS)).isEqualTo(LocalDate.of(2026, 9, 25));
    }

    @Test
    void daytimeWindowIncludesBothEnds() {
        LocalTime open = LocalTime.of(9, 0), close = LocalTime.of(17, 0);
        assertThat(BusinessCalendar.isWithin(LocalTime.of(9, 0), open, close)).isTrue();
        assertThat(BusinessCalendar.isWithin(LocalTime.of(17, 0), open, close)).isTrue();
        assertThat(BusinessCalendar.isWithin(LocalTime.of(8, 59, 59), open, close)).isFalse();
        assertThat(BusinessCalendar.isWithin(LocalTime.of(17, 0, 1), open, close)).isFalse();
    }

    @Test
    void windowClosingBeforeItOpensCrossesMidnight() {
        LocalTime open = LocalTime.of(22, 0), close = LocalTime.of(2, 0);
        assertThat(BusinessCalendar.isWithin(LocalTime.of(23, 0), open, close)).isTrue();
        assertThat(BusinessCalendar.isWithin(LocalTime.MIDNIGHT, open, close)).isTrue();
        assertThat(BusinessCalendar.isWithin(LocalTime.of(2, 0), open, close)).isTrue();
        assertThat(BusinessCalendar.isWithin(LocalTime.of(12, 0), open, close)).isFalse();
        assertThat(BusinessCalendar.isWithin(LocalTime.of(21, 59), open, close)).isFalse();
    }

    @Test
    void aWindowMustEndAfterItStarts() {
        Instant t = Instant.parse("2026-09-25T00:00:00Z");
        assertThatThrownBy(() -> new TimeWindow(t, t)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new TimeWindow(t, t.minusSeconds(1))).isInstanceOf(IllegalArgumentException.class);
    }
}
