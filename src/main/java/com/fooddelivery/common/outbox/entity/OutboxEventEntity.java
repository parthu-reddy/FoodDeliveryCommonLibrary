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
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload")
    private String payload;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    @Column(name = "status")
    private OutboxStatus status;
    @Column(name = "processed_at")
    private LocalDateTime processedAt;
    @Column(name = "error_message")
    private String errorMessage;
    @Column(name = "retry_count")
    private Integer retryCount;

    @java.lang.SuppressWarnings("all")
    private static OutboxStatus $default$status() {
        return OutboxStatus.UNPROCESSED;
    }

    @java.lang.SuppressWarnings("all")
    private static Integer $default$retryCount() {
        return 0;
    }


    @java.lang.SuppressWarnings("all")
    public static class OutboxEventEntityBuilder {
        @java.lang.SuppressWarnings("all")
        private UUID id;
        @java.lang.SuppressWarnings("all")
        private com.fooddelivery.common.constants.AggregateType aggregateType;
        @java.lang.SuppressWarnings("all")
        private String aggregateId;
        @java.lang.SuppressWarnings("all")
        private com.fooddelivery.common.constants.EventType eventType;
        @java.lang.SuppressWarnings("all")
        private String payload;
        @java.lang.SuppressWarnings("all")
        private LocalDateTime createdAt;
        @java.lang.SuppressWarnings("all")
        private boolean status$set;
        @java.lang.SuppressWarnings("all")
        private OutboxStatus status$value;
        @java.lang.SuppressWarnings("all")
        private LocalDateTime processedAt;
        @java.lang.SuppressWarnings("all")
        private String errorMessage;
        @java.lang.SuppressWarnings("all")
        private boolean retryCount$set;
        @java.lang.SuppressWarnings("all")
        private Integer retryCount$value;

        @java.lang.SuppressWarnings("all")
        OutboxEventEntityBuilder() {
        }

        /**
         * @return {@code this}.
         */
        @java.lang.SuppressWarnings("all")
        public OutboxEventEntity.OutboxEventEntityBuilder id(final UUID id) {
            this.id = id;
            return this;
        }

        /**
         * @return {@code this}.
         */
        @java.lang.SuppressWarnings("all")
        public OutboxEventEntity.OutboxEventEntityBuilder aggregateType(final com.fooddelivery.common.constants.AggregateType aggregateType) {
            this.aggregateType = aggregateType;
            return this;
        }

        /**
         * @return {@code this}.
         */
        @java.lang.SuppressWarnings("all")
        public OutboxEventEntity.OutboxEventEntityBuilder aggregateId(final String aggregateId) {
            this.aggregateId = aggregateId;
            return this;
        }

        /**
         * @return {@code this}.
         */
        @java.lang.SuppressWarnings("all")
        public OutboxEventEntity.OutboxEventEntityBuilder eventType(final com.fooddelivery.common.constants.EventType eventType) {
            this.eventType = eventType;
            return this;
        }

        /**
         * @return {@code this}.
         */
        @java.lang.SuppressWarnings("all")
        public OutboxEventEntity.OutboxEventEntityBuilder payload(final String payload) {
            this.payload = payload;
            return this;
        }

        /**
         * @return {@code this}.
         */
        @java.lang.SuppressWarnings("all")
        public OutboxEventEntity.OutboxEventEntityBuilder createdAt(final LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        /**
         * @return {@code this}.
         */
        @java.lang.SuppressWarnings("all")
        public OutboxEventEntity.OutboxEventEntityBuilder status(final OutboxStatus status) {
            this.status$value = status;
            status$set = true;
            return this;
        }

        /**
         * @return {@code this}.
         */
        @java.lang.SuppressWarnings("all")
        public OutboxEventEntity.OutboxEventEntityBuilder processedAt(final LocalDateTime processedAt) {
            this.processedAt = processedAt;
            return this;
        }

        /**
         * @return {@code this}.
         */
        @java.lang.SuppressWarnings("all")
        public OutboxEventEntity.OutboxEventEntityBuilder errorMessage(final String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        /**
         * @return {@code this}.
         */
        @java.lang.SuppressWarnings("all")
        public OutboxEventEntity.OutboxEventEntityBuilder retryCount(final Integer retryCount) {
            this.retryCount$value = retryCount;
            retryCount$set = true;
            return this;
        }

        @java.lang.SuppressWarnings("all")
        public OutboxEventEntity build() {
            OutboxStatus status$value = this.status$value;
            if (!this.status$set) status$value = OutboxEventEntity.$default$status();
            Integer retryCount$value = this.retryCount$value;
            if (!this.retryCount$set) retryCount$value = OutboxEventEntity.$default$retryCount();
            return new OutboxEventEntity(this.id, this.aggregateType, this.aggregateId, this.eventType, this.payload, this.createdAt, status$value, this.processedAt, this.errorMessage, retryCount$value);
        }

        @java.lang.Override
        @java.lang.SuppressWarnings("all")
        public java.lang.String toString() {
            return "OutboxEventEntity.OutboxEventEntityBuilder(id=" + this.id + ", aggregateType=" + this.aggregateType + ", aggregateId=" + this.aggregateId + ", eventType=" + this.eventType + ", payload=" + this.payload + ", createdAt=" + this.createdAt + ", status$value=" + this.status$value + ", processedAt=" + this.processedAt + ", errorMessage=" + this.errorMessage + ", retryCount$value=" + this.retryCount$value + ")";
        }
    }

    @java.lang.SuppressWarnings("all")
    public static OutboxEventEntity.OutboxEventEntityBuilder builder() {
        return new OutboxEventEntity.OutboxEventEntityBuilder();
    }

    @java.lang.SuppressWarnings("all")
    public UUID getId() {
        return this.id;
    }

    @java.lang.SuppressWarnings("all")
    public com.fooddelivery.common.constants.AggregateType getAggregateType() {
        return this.aggregateType;
    }

    @java.lang.SuppressWarnings("all")
    public String getAggregateId() {
        return this.aggregateId;
    }

    @java.lang.SuppressWarnings("all")
    public com.fooddelivery.common.constants.EventType getEventType() {
        return this.eventType;
    }

    @java.lang.SuppressWarnings("all")
    public String getPayload() {
        return this.payload;
    }

    @java.lang.SuppressWarnings("all")
    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    @java.lang.SuppressWarnings("all")
    public OutboxStatus getStatus() {
        return this.status;
    }

    @java.lang.SuppressWarnings("all")
    public LocalDateTime getProcessedAt() {
        return this.processedAt;
    }

    @java.lang.SuppressWarnings("all")
    public String getErrorMessage() {
        return this.errorMessage;
    }

    @java.lang.SuppressWarnings("all")
    public Integer getRetryCount() {
        return this.retryCount;
    }

    @java.lang.SuppressWarnings("all")
    public void setId(final UUID id) {
        this.id = id;
    }

    @java.lang.SuppressWarnings("all")
    public void setAggregateType(final com.fooddelivery.common.constants.AggregateType aggregateType) {
        this.aggregateType = aggregateType;
    }

    @java.lang.SuppressWarnings("all")
    public void setAggregateId(final String aggregateId) {
        this.aggregateId = aggregateId;
    }

    @java.lang.SuppressWarnings("all")
    public void setEventType(final com.fooddelivery.common.constants.EventType eventType) {
        this.eventType = eventType;
    }

    @java.lang.SuppressWarnings("all")
    public void setPayload(final String payload) {
        this.payload = payload;
    }

    @java.lang.SuppressWarnings("all")
    public void setCreatedAt(final LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @java.lang.SuppressWarnings("all")
    public void setStatus(final OutboxStatus status) {
        this.status = status;
    }

    @java.lang.SuppressWarnings("all")
    public void setProcessedAt(final LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }

    @java.lang.SuppressWarnings("all")
    public void setErrorMessage(final String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @java.lang.SuppressWarnings("all")
    public void setRetryCount(final Integer retryCount) {
        this.retryCount = retryCount;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof OutboxEventEntity)) return false;
        final OutboxEventEntity other = (OutboxEventEntity) o;
        if (!other.canEqual((java.lang.Object) this)) return false;
        final java.lang.Object this$retryCount = this.getRetryCount();
        final java.lang.Object other$retryCount = other.getRetryCount();
        if (this$retryCount == null ? other$retryCount != null : !this$retryCount.equals(other$retryCount)) return false;
        final java.lang.Object this$id = this.getId();
        final java.lang.Object other$id = other.getId();
        if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
        final java.lang.Object this$aggregateType = this.getAggregateType();
        final java.lang.Object other$aggregateType = other.getAggregateType();
        if (this$aggregateType == null ? other$aggregateType != null : !this$aggregateType.equals(other$aggregateType)) return false;
        final java.lang.Object this$aggregateId = this.getAggregateId();
        final java.lang.Object other$aggregateId = other.getAggregateId();
        if (this$aggregateId == null ? other$aggregateId != null : !this$aggregateId.equals(other$aggregateId)) return false;
        final java.lang.Object this$eventType = this.getEventType();
        final java.lang.Object other$eventType = other.getEventType();
        if (this$eventType == null ? other$eventType != null : !this$eventType.equals(other$eventType)) return false;
        final java.lang.Object this$payload = this.getPayload();
        final java.lang.Object other$payload = other.getPayload();
        if (this$payload == null ? other$payload != null : !this$payload.equals(other$payload)) return false;
        final java.lang.Object this$createdAt = this.getCreatedAt();
        final java.lang.Object other$createdAt = other.getCreatedAt();
        if (this$createdAt == null ? other$createdAt != null : !this$createdAt.equals(other$createdAt)) return false;
        final java.lang.Object this$status = this.getStatus();
        final java.lang.Object other$status = other.getStatus();
        if (this$status == null ? other$status != null : !this$status.equals(other$status)) return false;
        final java.lang.Object this$processedAt = this.getProcessedAt();
        final java.lang.Object other$processedAt = other.getProcessedAt();
        if (this$processedAt == null ? other$processedAt != null : !this$processedAt.equals(other$processedAt)) return false;
        final java.lang.Object this$errorMessage = this.getErrorMessage();
        final java.lang.Object other$errorMessage = other.getErrorMessage();
        if (this$errorMessage == null ? other$errorMessage != null : !this$errorMessage.equals(other$errorMessage)) return false;
        return true;
    }

    @java.lang.SuppressWarnings("all")
    protected boolean canEqual(final java.lang.Object other) {
        return other instanceof OutboxEventEntity;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final java.lang.Object $retryCount = this.getRetryCount();
        result = result * PRIME + ($retryCount == null ? 43 : $retryCount.hashCode());
        final java.lang.Object $id = this.getId();
        result = result * PRIME + ($id == null ? 43 : $id.hashCode());
        final java.lang.Object $aggregateType = this.getAggregateType();
        result = result * PRIME + ($aggregateType == null ? 43 : $aggregateType.hashCode());
        final java.lang.Object $aggregateId = this.getAggregateId();
        result = result * PRIME + ($aggregateId == null ? 43 : $aggregateId.hashCode());
        final java.lang.Object $eventType = this.getEventType();
        result = result * PRIME + ($eventType == null ? 43 : $eventType.hashCode());
        final java.lang.Object $payload = this.getPayload();
        result = result * PRIME + ($payload == null ? 43 : $payload.hashCode());
        final java.lang.Object $createdAt = this.getCreatedAt();
        result = result * PRIME + ($createdAt == null ? 43 : $createdAt.hashCode());
        final java.lang.Object $status = this.getStatus();
        result = result * PRIME + ($status == null ? 43 : $status.hashCode());
        final java.lang.Object $processedAt = this.getProcessedAt();
        result = result * PRIME + ($processedAt == null ? 43 : $processedAt.hashCode());
        final java.lang.Object $errorMessage = this.getErrorMessage();
        result = result * PRIME + ($errorMessage == null ? 43 : $errorMessage.hashCode());
        return result;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public java.lang.String toString() {
        return "OutboxEventEntity(id=" + this.getId() + ", aggregateType=" + this.getAggregateType() + ", aggregateId=" + this.getAggregateId() + ", eventType=" + this.getEventType() + ", payload=" + this.getPayload() + ", createdAt=" + this.getCreatedAt() + ", status=" + this.getStatus() + ", processedAt=" + this.getProcessedAt() + ", errorMessage=" + this.getErrorMessage() + ", retryCount=" + this.getRetryCount() + ")";
    }

    @java.lang.SuppressWarnings("all")
    public OutboxEventEntity() {
        this.status = OutboxEventEntity.$default$status();
        this.retryCount = OutboxEventEntity.$default$retryCount();
    }

    @java.lang.SuppressWarnings("all")
    public OutboxEventEntity(final UUID id, final com.fooddelivery.common.constants.AggregateType aggregateType, final String aggregateId, final com.fooddelivery.common.constants.EventType eventType, final String payload, final LocalDateTime createdAt, final OutboxStatus status, final LocalDateTime processedAt, final String errorMessage, final Integer retryCount) {
        this.id = id;
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.payload = payload;
        this.createdAt = createdAt;
        this.status = status;
        this.processedAt = processedAt;
        this.errorMessage = errorMessage;
        this.retryCount = retryCount;
    }
}
