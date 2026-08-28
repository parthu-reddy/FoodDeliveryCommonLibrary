package com.fooddelivery.common.idempotency;

import com.fooddelivery.common.repository.IIdempotencyKeyRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Deletes idempotency keys past their retention window, for every service that writes them.
 *
 * <p>Consumers claim a key per handled event ({@code tryClaim}, or a {@code save} of an
 * {@link com.fooddelivery.common.entity.IdempotencyKey}) and nothing removed them. Measured
 * 2026-08-27: <strong>eleven services write keys, three swept them</strong> — and the three did it
 * with byte-identical 29-line copies of the same class.
 *
 * <p>Centralised here rather than copied an eighth time, for the same reason
 * {@code OutboxProcessor} lives in this library: a service that writes keys should get the cleanup
 * without having to remember, and a new service should inherit it. The per-service copies in
 * CustomerApplication, RestaurantApplication and DeliveryExecutiveApplication were deleted.
 *
 * <p><strong>@replication-safe: idempotent.</strong> Deleting rows older than a cutoff produces the
 * same result whether it runs once or five times concurrently, so this adds no constraint to the
 * single-replica question tracked in Phase 7. Stated explicitly because it is the first question
 * anyone will ask on seeing another {@code @Scheduled}.
 *
 * <p>The repository is taken as an {@link ObjectProvider} rather than a hard dependency: several
 * services have a datasource but never touch idempotency keys, and they must not fail to start over
 * a bean they do not use.
 */
@Slf4j
public class IdempotencyKeySweeper {

    /** Matches the retention the three hand-written copies used. */
    static final int RETENTION_DAYS = 7;

    private final ObjectProvider<IIdempotencyKeyRepository> repositoryProvider;

    public IdempotencyKeySweeper(ObjectProvider<IIdempotencyKeyRepository> repositoryProvider) {
        this.repositoryProvider = repositoryProvider;
    }

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void sweepExpiredKeys() {
        IIdempotencyKeyRepository repository = repositoryProvider.getIfAvailable();
        if (repository == null) {
            return;
        }
        LocalDateTime cutoff = LocalDateTime.now().minusDays(RETENTION_DAYS);
        try {
            int deleted = repository.deleteOlderThan(cutoff);
            if (deleted > 0) {
                log.info("Swept {} idempotency keys older than {}", deleted, cutoff);
            }
        } catch (Exception e) {
            // Never let housekeeping take down the scheduler for the other jobs in this service.
            log.error("Failed to sweep idempotency keys older than {}", cutoff, e);
        }
    }
}
