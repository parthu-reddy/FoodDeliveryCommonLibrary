package com.fooddelivery.common.time;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

/**
 * Calendar arithmetic in an explicit zone. Every method takes the zone as a parameter, and
 * nothing here reads a default.
 *
 * <p>Which zone to pass is a business decision, made where the data is:
 * <ul>
 *   <li>an outlet's {@code time_zone} for its opening hours;</li>
 *   <li>an advertiser's {@code time_zone} for daily budgets, dayparting and reporting days;</li>
 *   <li>{@code platform.accounting-zone} for the day the books reconcile;</li>
 *   <li>the viewer's browser zone for what a person sees, which the UI computes and sends as a
 *       {@link TimeWindow}.</li>
 * </ul>
 */
public final class BusinessCalendar {

    private BusinessCalendar() {}

    /** The calendar date it currently is in {@code zone}. */
    public static LocalDate today(Clock clock, ZoneId zone) {
        return LocalDate.ofInstant(clock.instant(), zone);
    }

    /** The calendar date {@code instant} falls on in {@code zone}. */
    public static LocalDate localDate(Instant instant, ZoneId zone) {
        return LocalDate.ofInstant(instant, zone);
    }

    /** The wall-clock time {@code instant} shows in {@code zone}. */
    public static LocalTime localTime(Instant instant, ZoneId zone) {
        return LocalTime.ofInstant(instant, zone);
    }

    /**
     * The first instant of {@code date} in {@code zone}. Where midnight does not exist (a DST gap
     * at 00:00) this is the first instant that does, per {@link LocalDate#atStartOfDay(ZoneId)}.
     */
    public static Instant startOfDay(LocalDate date, ZoneId zone) {
        return date.atStartOfDay(zone).toInstant();
    }

    /** {@code date} in {@code zone} as {@code [start of date, start of next date)}: 23, 24 or 25 hours. */
    public static TimeWindow day(LocalDate date, ZoneId zone) {
        return new TimeWindow(startOfDay(date, zone), startOfDay(date.plusDays(1), zone));
    }

    /**
     * How much of its local day has passed at {@code instant}, in {@code [0, 1)}.
     *
     * <p>Divides by the real length of that local day, so on a 25-hour day the fraction does not
     * reach 1 an hour before midnight, and on a 23-hour day it does not stop at 0.96.
     */
    public static double fractionOfDayElapsed(Instant instant, ZoneId zone) {
        TimeWindow day = day(localDate(instant, zone), zone);
        return (double) Duration.between(day.from(), instant).toMillis() / day.length().toMillis();
    }

    /**
     * Whether wall-clock {@code time} is inside the daily window {@code [open, close]}. Both ends are
     * inclusive, which is what every opening-hours check on the platform has always used. A window whose
     * {@code close} is before its {@code open} crosses midnight ({@code 22:00–02:00}).
     */
    public static boolean isWithin(LocalTime time, LocalTime open, LocalTime close) {
        if (!open.isAfter(close)) {
            return !time.isBefore(open) && !time.isAfter(close);
        }
        return !time.isBefore(open) || !time.isAfter(close);
    }
}
