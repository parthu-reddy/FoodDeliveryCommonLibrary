package com.fooddelivery.common.outbox.service;

import com.fooddelivery.common.enums.OutboxStatus;
import com.fooddelivery.common.outbox.repository.OutboxEventRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Exports {@code money_outbox_backlog_age_seconds}: how long the oldest unpublished outbox event has
 * been waiting.
 *
 * <p>Every money movement leaves a service through the outbox -- ledger entries, refund requests,
 * payout events. If the publisher stalls, none of them happen, and until now nothing measured it:
 * the metric was named in the Phase 7 plan and never registered by anything.
 *
 * <p>Age rather than depth on purpose. A deep outbox that is draining is healthy; a shallow one
 * whose oldest row is an hour old is not, and depth alone cannot tell them apart.
 *
 * <p><strong>@replication-safe: idempotent</strong> -- reads one aggregate and publishes it as a
 * gauge. Every replica exporting its own view of the same query is correct, and Prometheus scrapes
 * them all; a lock would leave every replica but one reporting a stale zero.
 */
@Component
@Slf4j
public class OutboxBacklogMetrics {

    private static final List<OutboxStatus> PENDING =
            List.of(OutboxStatus.UNPROCESSED, OutboxStatus.FAILED);

    private final OutboxEventRepository outboxEventRepository;
    private final AtomicLong backlogAgeSeconds = new AtomicLong(0);

    public OutboxBacklogMetrics(OutboxEventRepository outboxEventRepository, MeterRegistry meterRegistry) {
        this.outboxEventRepository = outboxEventRepository;
        Gauge.builder("money_outbox_backlog_age_seconds", backlogAgeSeconds, AtomicLong::doubleValue)
             .description("Age in seconds of the oldest outbox event that has not been published")
             .register(meterRegistry);
    }

    @Scheduled(fixedDelayString = "${money.outbox-backlog.refresh-ms:30000}")
    public void refresh() {
        try {
            backlogAgeSeconds.set(oldestPendingAgeSeconds());
        } catch (Exception e) {
            log.warn("Could not refresh the outbox backlog metric: {}", e.getMessage());
        }
    }

    long oldestPendingAgeSeconds() {
        LocalDateTime oldest = outboxEventRepository.findOldestPendingCreatedAt(PENDING);
        if (oldest == null) {
            return 0L;
        }
        return Math.max(0L, Duration.between(oldest, LocalDateTime.now()).getSeconds());
    }
}
