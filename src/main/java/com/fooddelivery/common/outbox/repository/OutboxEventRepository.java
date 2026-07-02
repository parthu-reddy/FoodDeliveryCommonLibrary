package com.fooddelivery.common.outbox.repository;

import com.fooddelivery.common.outbox.entity.OutboxEventEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxEventEntity, UUID> {
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(value = "SELECT o FROM CommonOutboxEventEntity o WHERE o.status IN :statuses ORDER BY o.createdAt ASC LIMIT 100")
    List<OutboxEventEntity> findUnprocessedEventsAndLock(@Param("statuses") List<String> statuses);
}
