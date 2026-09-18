package com.fooddelivery.common.lock;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * Times a scheduled job that runs under a Redis lock, and says so when the work approaches the
 * lock's TTL.
 *
 * <p>A lock TTL has to exceed how long the work actually takes, or the lock expires mid-run and the
 * mutual exclusion it advertises is fiction. {@code DriverPingTimeoutPoller} held a 4-second lock
 * while a single poll could make fifty Feign calls and sleep three seconds per unreachable driver;
 * it lost that race routinely and nothing said so.
 *
 * <p>The TTLs were then raised to 60s by reasoning rather than measurement, which is the same
 * standard that produced the 4. Measuring properly needs a deployed system under real backlog. This
 * closes the gap from the other side: every run is timed, and a run that uses more than
 * {@link #WARN_FRACTION} of its TTL logs a warning naming both numbers. The evidence accumulates
 * wherever the job actually runs, instead of waiting for a load test that needs an environment
 * nobody has yet.
 *
 * <p>Metrics, per job name:
 * <ul>
 *   <li>{@code scheduled.locked.work} — timer of the work inside the lock</li>
 *   <li>{@code scheduled.locked.work.ttl_pressure} — counter, incremented when a run exceeds the
 *       warn fraction. A non-zero value here is the signal to raise the TTL, and the timer's max is
 *       the number to raise it to.</li>
 * </ul>
 */
public final class LockedWorkTimer {

    private static final Logger log = LoggerFactory.getLogger(LockedWorkTimer.class);

    /** Warn once a run has consumed this much of its lock TTL. */
    public static final double WARN_FRACTION = 0.5;

    private LockedWorkTimer() {
    }

    /**
     * Runs {@code work}, timing it against {@code ttl}.
     *
     * @param registry may be null; the work still runs, it is simply not recorded
     * @param job      metric tag and log identifier, e.g. "driverPingTimeoutPoller"
     * @param ttl      the TTL of the lock held for the duration of {@code work}
     */
    public static void timed(MeterRegistry registry, String job, Duration ttl, Runnable work) {
        long startNanos = System.nanoTime();
        try {
            work.run();
        } finally {
            long elapsedNanos = System.nanoTime() - startNanos;
            Duration elapsed = Duration.ofNanos(elapsedNanos);
            if (registry != null) {
                Timer.builder("scheduled.locked.work")
                        .tag("job", job)
                        .register(registry)
                        .record(elapsed);
            }
            long ttlMs = ttl.toMillis();
            if (ttlMs > 0 && elapsed.toMillis() > ttlMs * WARN_FRACTION) {
                // Not an error: the run may well have finished inside the TTL. It is the margin
                // that is shrinking, and the margin is the only thing making the lock meaningful.
                log.warn("LOCK_TTL_PRESSURE job={} elapsedMs={} ttlMs={} fractionUsed={} -- "
                                + "raise the TTL above the observed maximum, or bound the work further",
                        job, elapsed.toMillis(), ttlMs,
                        String.format("%.2f", (double) elapsed.toMillis() / ttlMs));
                if (registry != null) {
                    registry.counter("scheduled.locked.work.ttl_pressure", "job", job).increment();
                }
            }
        }
    }
}
