package com.fooddelivery.common.lock;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The scheduler TTLs were set to 60s by reasoning, not measurement — the same standard that produced
 * the 4s they replaced. Measuring properly needs a deployed system under real backlog, which does
 * not exist while deployment is parked, so this closes the gap from the other side: every locked run
 * times itself and says so when it eats into its own TTL.
 */
class LockedWorkTimerTest {

    @Test
    void aQuickRunRecordsATimingAndNoPressure() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();

        LockedWorkTimer.timed(registry, "quickJob", Duration.ofSeconds(60), () -> { });

        assertThat(registry.find("scheduled.locked.work").tag("job", "quickJob").timer().count())
                .isEqualTo(1);
        assertThat(registry.find("scheduled.locked.work.ttl_pressure").tag("job", "quickJob").counter())
                .describedAs("a run well inside its TTL must not raise pressure")
                .isNull();
    }

    /** The case that matters: work approaching the TTL, while the lock still technically holds. */
    @Test
    void aRunThatEatsIntoItsTtlRaisesPressure() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();

        // TTL deliberately tiny so a real sleep crosses the warn fraction without a slow test.
        LockedWorkTimer.timed(registry, "slowJob", Duration.ofMillis(20), () -> {
            try {
                Thread.sleep(40);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        assertThat(registry.find("scheduled.locked.work.ttl_pressure").tag("job", "slowJob").counter())
                .describedAs("exceeding the warn fraction of the TTL must be counted, not just logged")
                .isNotNull();
        assertThat(registry.find("scheduled.locked.work.ttl_pressure").tag("job", "slowJob")
                .counter().count()).isEqualTo(1.0);
    }

    /** Timing must not swallow a failure, and must still record the run that failed. */
    @Test
    void aFailingRunStillRecordsAndStillThrows() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();

        assertThatThrownBy(() -> LockedWorkTimer.timed(registry, "brokenJob", Duration.ofSeconds(60),
                () -> { throw new IllegalStateException("boom"); }))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("boom");

        assertThat(registry.find("scheduled.locked.work").tag("job", "brokenJob").timer().count())
                .isEqualTo(1);
    }

    /** A consumer without a registry still gets the work done and the warning logged. */
    @Test
    void aNullRegistryIsTolerated() {
        LockedWorkTimer.timed(null, "noRegistry", Duration.ofMillis(1), () -> {
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }
}
