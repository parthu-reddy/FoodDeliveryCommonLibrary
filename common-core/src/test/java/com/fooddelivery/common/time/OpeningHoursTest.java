package com.fooddelivery.common.time;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Defect D3 (RandomDocuments/TimezoneCorrectness_2026-09-25): opening hours were evaluated on an
 * Indian clock for every outlet. Runs in Pacific/Chatham, so no case can pass by the JVM zone
 * happening to agree.
 */
class OpeningHoursTest {

    private static final ZoneId NEW_YORK = ZoneId.of("America/New_York");
    private static final ZoneId KOLKATA = ZoneId.of("Asia/Kolkata");

    /** A daily window, standing in for an outlet's or a category's timing row. */
    private record Window(LocalTime opens, LocalTime closes) { }

    private static Window window(int open, int close) {
        return new Window(LocalTime.of(open, 0), LocalTime.of(close, 0));
    }

    private static boolean open(Instant now, ZoneId zone, Window... windows) {
        return OpeningHours.isOpen(now, zone, List.of(windows), Window::opens, Window::closes);
    }

    @Test
    void aNewYorkOutletKeepsNewYorkHours() {
        Window nineToFive = window(9, 17);
        // 13:00Z is 09:00 in New York (EDT) and 18:30 in Kolkata.
        assertTrue(open(Instant.parse("2026-09-25T13:00:00Z"), NEW_YORK, nineToFive));
        // 03:30Z is 09:00 in Kolkata and 23:30 the night before in New York.
        assertFalse(open(Instant.parse("2026-09-25T03:30:00Z"), NEW_YORK, nineToFive));
        assertTrue(open(Instant.parse("2026-09-25T03:30:00Z"), KOLKATA, nineToFive));
    }

    @Test
    void aLateNightWindowCrossesMidnightInTheOutletsZone() {
        Window lateNight = window(22, 2);
        assertTrue(open(Instant.parse("2026-09-25T04:30:00Z"), NEW_YORK, lateNight));   // 00:30 in New York
        assertFalse(open(Instant.parse("2026-09-25T16:00:00Z"), NEW_YORK, lateNight));  // 12:00 in New York
    }

    @Test
    void theWallClockFollowsDaylightSaving() {
        Window nineToFive = window(9, 17);
        // 13:30Z is 09:30 in New York in summer (EDT, -4) but 08:30 in winter (EST, -5).
        assertTrue(open(Instant.parse("2026-07-01T13:30:00Z"), NEW_YORK, nineToFive));
        assertFalse(open(Instant.parse("2026-12-01T13:30:00Z"), NEW_YORK, nineToFive));
    }

    /** 09:00 Kolkata on 2026-09-25, plus a wall-clock offset; Kolkata has no daylight saving. */
    private static Instant kolkata(int hour, int minute, int second, int nanos) {
        return Instant.parse("2026-09-25T03:30:00Z").plusSeconds((hour - 9) * 3600L + minute * 60L + second).plusNanos(nanos);
    }

    private static Window window(LocalTime open, LocalTime close) {
        return new Window(open, close);
    }

    @Test
    void closingInstantIsClosed() {
        Window nineToTen = window(9, 22);
        assertTrue(open(kolkata(9, 0, 0, 0), KOLKATA, nineToTen));
        assertTrue(open(kolkata(21, 59, 59, 999_999_999), KOLKATA, nineToTen));
        assertFalse(open(kolkata(22, 0, 0, 0), KOLKATA, nineToTen));
        assertFalse(open(kolkata(8, 59, 59, 999_999_999), KOLKATA, nineToTen));
    }

    @Test
    void equalOpenAndCloseMeansAllDay() {
        Window allDay = window(LocalTime.MIDNIGHT, LocalTime.MIDNIGHT);
        assertTrue(open(kolkata(0, 0, 0, 0), KOLKATA, allDay));
        assertTrue(open(kolkata(12, 0, 0, 0), KOLKATA, allDay));
        assertTrue(open(kolkata(23, 59, 59, 999_999_999), KOLKATA, allDay));
    }

    @Test
    void crossingMidnightClosesAtItsClosingInstant() {
        Window lateNight = window(22, 2);
        assertTrue(open(kolkata(22, 0, 0, 0), KOLKATA, lateNight));
        assertTrue(open(kolkata(1, 59, 59, 999_999_999), KOLKATA, lateNight));
        assertFalse(open(kolkata(2, 0, 0, 0), KOLKATA, lateNight));
        assertFalse(open(kolkata(21, 59, 59, 999_999_999), KOLKATA, lateNight));
    }

    @Test
    void anyEqualPairIsAllDay() {
        Window tenToTen = window(LocalTime.of(10, 0), LocalTime.of(10, 0));
        assertTrue(open(kolkata(9, 59, 59, 0), KOLKATA, tenToTen));
        assertTrue(open(kolkata(10, 0, 0, 0), KOLKATA, tenToTen));
        assertTrue(open(kolkata(3, 0, 0, 0), KOLKATA, tenToTen));
    }

    @Test
    void noHoursMeansClosed() {
        assertFalse(OpeningHours.isOpen(Instant.now(), KOLKATA, List.<Window>of(), Window::opens, Window::closes));
    }
}
