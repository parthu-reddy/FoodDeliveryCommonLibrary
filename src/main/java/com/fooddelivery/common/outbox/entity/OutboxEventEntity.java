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
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "CommonOutboxEventEntity")
@Table(name = "outbox_events")
public class OutboxEventEntity {
    @Id
    @Column(name = "id")
    private UUID id;
    
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
    private LocalDateTime createdAt;
    
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
}
