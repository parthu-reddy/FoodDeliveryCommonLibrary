package com.fooddelivery.common.time;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.function.Function;

/**
 * Whether an outlet, or one of its menu categories, is open at an instant.
 *
 * <p>Lives in CommonLibrary since 2026-10-14 (decomposition D-3): restaurant-service evaluates outlet hours and
 * catalog-service evaluates outlet and category hours, and both must agree to the instant.
 *
 * <p>Opening hours are wall-clock times ({@code 09:00–22:00}) in the outlet's own zone, so the
 * instant is converted to that zone's wall clock first. This used to be four hand-copied loops
 * against {@code LocalTime.now(ZoneId.of("Asia/Kolkata"))}, which was right for Bengaluru and wrong for
 * every other outlet. RandomDocuments/TimezoneCorrectness_2026-09-25, defect D3.
 *
 * <p>A window is half-open, {@code [open, close)}: open at its opening instant, closed at its closing
 * instant, so {@code 09:00–12:00} and {@code 12:00–15:00} meet without overlapping. A window whose close
 * is before its open crosses midnight ({@code 22:00–02:00}). A window whose open equals its close
 * ({@code 00:00–00:00}) is open all day: that is the one way to say "all day", because a {@code LocalTime}
 * has no 24:00 and any window ending at 23:59 or 23:59:59 would close before midnight.
 * RandomDocuments/TimeIndependentOrdering_2026-10-09, F1.
 */
public final class OpeningHours {

    private OpeningHours() {}

    /** True if {@code now}, read on the outlet's wall clock, falls inside any of {@code windows}. */
    public static <T> boolean isOpen(Instant now, ZoneId outletZone, Collection<T> windows,
                                     Function<T, LocalTime> opens, Function<T, LocalTime> closes) {
        if (windows == null || windows.isEmpty()) {
            return false;
        }
        LocalTime wallClock = BusinessCalendar.localTime(now, outletZone);
        for (T window : windows) {
            if (contains(wallClock, opens.apply(window), closes.apply(window))) {
                return true;
            }
        }
        return false;
    }

    /** Whether wall-clock {@code time} is inside the daily window {@code [open, close)}; see the class comment. */
    static boolean contains(LocalTime time, LocalTime open, LocalTime close) {
        if (open.equals(close)) {
            return true;
        }
        if (open.isBefore(close)) {
            return !time.isBefore(open) && time.isBefore(close);
        }
        return !time.isBefore(open) || time.isBefore(close);
    }
}
