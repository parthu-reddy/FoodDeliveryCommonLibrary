package com.fooddelivery.common.outbox;

import com.fooddelivery.common.enums.OutboxStatus;
import com.fooddelivery.common.outbox.repository.OutboxEventRepository;
import com.fooddelivery.common.outbox.service.OutboxBacklogMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * The outbox backlog is the age of the oldest unpublished event, because every money movement
 * leaves a service through the outbox.
 *
 * <p>{@code money_outbox_backlog_age_seconds} was named in the Phase 7 plan and registered by
 * nothing, so a stalled publisher -- no ledger entries, no refund requests, no payout events --
 * was invisible.
 */
public class OutboxBacklogMetricsTest {

    private OutboxEventRepository repository;
    private MeterRegistry registry;
    private OutboxBacklogMetrics metrics;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(OutboxEventRepository.class);
        registry = new SimpleMeterRegistry();
        metrics = new OutboxBacklogMetrics(repository, registry);
    }

    @Test
    void theGaugeIsRegisteredUnderTheNameTheAlertQueries() {
        assertNotNull(registry.find("money_outbox_backlog_age_seconds").gauge(),
                "the MoneyOutboxBacklogStalled rule reads this exact series");
    }

    @Test
    void anEmptyOutboxReadsZero() {
        when(repository.findOldestPendingCreatedAt(any())).thenReturn(null);

        metrics.refresh();

        assertEquals(0.0, registry.find("money_outbox_backlog_age_seconds").gauge().value());
    }

    @Test
    void theGaugeIsTheAgeOfTheOldestPendingEvent() {
        when(repository.findOldestPendingCreatedAt(any()))
                .thenReturn(Instant.now().minus(java.time.Duration.ofMinutes(20)));

        metrics.refresh();

        double seconds = registry.find("money_outbox_backlog_age_seconds").gauge().value();
        assertTrue(seconds >= 1190 && seconds <= 1210, "expected about 1200s, got " + seconds);
    }

    /** Depth cannot tell a draining outbox from a stuck one; only the oldest row's age can. */
    @Test
    void bothUnprocessedAndFailedCountAsBacklog() {
        when(repository.findOldestPendingCreatedAt(any())).thenReturn(Instant.now());

        metrics.refresh();

        ArgumentCaptor<List<OutboxStatus>> statuses = ArgumentCaptor.forClass(List.class);
        Mockito.verify(repository).findOldestPendingCreatedAt(statuses.capture());
        assertTrue(statuses.getValue().contains(OutboxStatus.UNPROCESSED));
        assertTrue(statuses.getValue().contains(OutboxStatus.FAILED),
                "a FAILED event is still an unpublished money movement");
        assertTrue(!statuses.getValue().contains(OutboxStatus.PROCESSED));
    }

    /** An unreachable database must not take the metrics thread down with it. */
    @Test
    void aFailingQueryLeavesTheLastKnownValue() {
        when(repository.findOldestPendingCreatedAt(any())).thenReturn(Instant.now().minus(java.time.Duration.ofMinutes(5)));
        metrics.refresh();
        double before = registry.find("money_outbox_backlog_age_seconds").gauge().value();

        when(repository.findOldestPendingCreatedAt(any())).thenThrow(new RuntimeException("db down"));
        metrics.refresh();

        assertEquals(before, registry.find("money_outbox_backlog_age_seconds").gauge().value());
    }
}
