package com.fooddelivery.common.time;

import java.time.Duration;
import java.time.Instant;

/**
 * The instant a money movement happened, as its producer states it (ClearingReconResiduals C3–C6).
 *
 * <p>A ledger posting and a wallet transaction are dated on this instant, not on when they were written: a capture in the
 * last seconds of a day belongs to that day even when its command is consumed after midnight. Any past instant is
 * accepted, because a retried or replayed movement keeps its business date. A future one is a producer clock running
 * ahead, not a movement; {@link #MAX_AHEAD} is far above clock skew between containers and far below any accounting period.
 */
public final class EffectiveTime {

    public static final Duration MAX_AHEAD = Duration.ofMinutes(5);

    private EffectiveTime() {
    }

    /**
     * @param occurredAt the producer's instant
     * @param now        the checking service's clock
     * @return {@code occurredAt}
     * @throws IllegalArgumentException when it is missing or more than {@link #MAX_AHEAD} after {@code now}
     */
    public static Instant require(Instant occurredAt, Instant now) {
        if (occurredAt == null) {
            throw new IllegalArgumentException("A money movement states when it happened (occurredAt)");
        }
        if (occurredAt.isAfter(now.plus(MAX_AHEAD))) {
            throw new IllegalArgumentException("occurredAt " + occurredAt + " is more than " + MAX_AHEAD.toMinutes()
                    + " minutes after this service's clock " + now);
        }
        return occurredAt;
    }
}
