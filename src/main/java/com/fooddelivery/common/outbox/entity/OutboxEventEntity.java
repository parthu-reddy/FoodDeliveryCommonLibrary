package com.fooddelivery.common.outbox.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;
import java.util.UUID;
import com.fooddelivery.common.enums.OutboxStatus;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Persistable;
import jakarta.persistence.Transient;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PrePersist;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED, onConstructor_ = {@Deprecated})
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity(name = "CommonOutboxEventEntity")
@Table(name = "outbox_events")
@lombok.Getter
@lombok.Setter
public class OutboxEventEntity implements Persistable<UUID> {
    @Id
    @Column(name = "id")
    @Builder.Default
    private UUID id = UUID.randomUUID();
    
    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    @Column(name = "aggregate_type")
    private com.fooddelivery.common.constants.AggregateType aggregateType;
    
    @Column(name = "aggregate_id")
    private String aggregateId;
    
    @Column(name = "type")
    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    private com.fooddelivery.common.constants.EventType eventType;
    
    @Column(name = "idempotency_key", unique = true)
    private String idempotencyKey;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload")
    private String payload;
    
    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Builder.Default
    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    @Column(name = "status")
    private OutboxStatus status = OutboxStatus.UNPROCESSED;
    
    @Column(name = "processed_at")
    private LocalDateTime processedAt;
    
    @Column(name = "error_message")
    private String errorMessage;
    
    @Builder.Default
    @Column(name = "retry_count")
    private Integer retryCount = 0;

    @Transient
    @Builder.Default
    private boolean isNew = true;

    @PostPersist
    @PostLoad
    void markNotNew() {
        this.isNew = false;
    }
}
