package com.fooddelivery.common.time;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * A half-open interval of instants, {@code [from, to)}.
 *
 * <p>This is the only shape a "day" (or any calendar period) takes once it leaves the code that
 * knows its zone: in a query ({@code x >= :from AND x < :to}), in a request parameter, or across a
 * service boundary. A {@code LocalDate} can't make that trip, because every reader would have to
 * pick a zone to interpret it in, and the reconciliation queries used to pick the database
 * session's zone. The bounds are exclusive at the end so that consecutive windows tile without
 * overlap, and a day is 23, 24 or 25 hours long as the zone's rules dictate.
 */
public record TimeWindow(Instant from, Instant to) {

    public TimeWindow {
        Objects.requireNonNull(from, "from");
        Objects.requireNonNull(to, "to");
        if (!from.isBefore(to)) {
            throw new IllegalArgumentException("A time window must end after it starts: [" + from + ", " + to + ")");
        }
    }

    public boolean contains(Instant instant) {
        return !instant.isBefore(from) && instant.isBefore(to);
    }

    public Duration length() {
        return Duration.between(from, to);
    }
}
