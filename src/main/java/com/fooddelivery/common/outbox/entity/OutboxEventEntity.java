package com.fooddelivery.common.outbox.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;
import java.util.UUID;
import com.fooddelivery.common.enums.OutboxStatus;
@Entity(name = "CommonOutboxEventEntity")
@Table(name = "outbox_events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutboxEventEntity {
    @Id
    private UUID id;
    
    private String aggregateType;
    private String aggregateId;
    @jakarta.persistence.Column(name = "type")
    private String eventType;
    
    @JdbcTypeCode(SqlTypes.JSON)
    private String payload;
    
    private LocalDateTime createdAt;
    
    @Builder.Default
    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    private OutboxStatus status = OutboxStatus.UNPROCESSED;
    private LocalDateTime processedAt;
    private String errorMessage;
    
    @Builder.Default
    private Integer retryCount = 0;
}
