package com.fooddelivery.common.outbox.repository;

import com.fooddelivery.common.outbox.entity.OutboxEventEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;
import com.fooddelivery.common.enums.OutboxStatus;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEventEntity, UUID> {
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.QueryHints({@jakarta.persistence.QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2")})
    List<OutboxEventEntity> findTop100ByStatusInOrderByCreatedAtAsc(@Param("statuses") List<OutboxStatus> statuses);

    @org.springframework.data.jpa.repository.Modifying
    @Query("DELETE FROM CommonOutboxEventEntity o WHERE o.status = :status AND o.createdAt < :thresholdDate")
    int deleteProcessedEventsOlderThan(@Param("status") OutboxStatus status, @Param("thresholdDate") java.time.Instant thresholdDate);

    org.springframework.data.domain.Page<OutboxEventEntity> findByStatus(OutboxStatus status, org.springframework.data.domain.Pageable pageable);

    /**
     * When the oldest still-unpublished event was written. Its age is the outbox backlog: every
     * money movement in this service leaves through the outbox, so a growing age means ledger
     * entries, refunds and payouts are queued behind something and nobody can see it.
     */
    @Query("SELECT MIN(o.createdAt) FROM CommonOutboxEventEntity o WHERE o.status IN :statuses")
    java.time.Instant findOldestPendingCreatedAt(@Param("statuses") List<OutboxStatus> statuses);
}
