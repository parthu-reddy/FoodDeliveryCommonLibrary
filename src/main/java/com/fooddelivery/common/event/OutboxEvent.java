package com.fooddelivery.common.event;

import java.time.LocalDateTime;

public class OutboxEvent {
    private String id;
    private com.fooddelivery.common.constants.AggregateType aggregateType;
    private com.fooddelivery.common.constants.EventType eventType;
    private String aggregateId;
    private String type;
    private String payload;
    private LocalDateTime createdAt;


public static class OutboxEventBuilder {
private String id;
private com.fooddelivery.common.constants.AggregateType aggregateType;
private com.fooddelivery.common.constants.EventType eventType;
private String aggregateId;
private String type;
private String payload;
private LocalDateTime createdAt;

OutboxEventBuilder() {
        }

        /**
         * @return {@code this}.
         */
public OutboxEvent.OutboxEventBuilder id(final String id) {
            this.id = id;
            return this;
        }

        /**
         * @return {@code this}.
         */
public OutboxEvent.OutboxEventBuilder aggregateType(final com.fooddelivery.common.constants.AggregateType aggregateType) {
            this.aggregateType = aggregateType;
            return this;
        }

        /**
         * @return {@code this}.
         */
public OutboxEvent.OutboxEventBuilder eventType(final com.fooddelivery.common.constants.EventType eventType) {
            this.eventType = eventType;
            return this;
        }

        /**
         * @return {@code this}.
         */
public OutboxEvent.OutboxEventBuilder aggregateId(final String aggregateId) {
            this.aggregateId = aggregateId;
            return this;
        }

        /**
         * @return {@code this}.
         */
public OutboxEvent.OutboxEventBuilder type(final String type) {
            this.type = type;
            return this;
        }

        /**
         * @return {@code this}.
         */
public OutboxEvent.OutboxEventBuilder payload(final String payload) {
            this.payload = payload;
            return this;
        }

        /**
         * @return {@code this}.
         */
public OutboxEvent.OutboxEventBuilder createdAt(final LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

public OutboxEvent build() {
            return new OutboxEvent(this.id, this.aggregateType, this.eventType, this.aggregateId, this.type, this.payload, this.createdAt);
        }

        @java.lang.Override
public java.lang.String toString() {
            return "OutboxEvent.OutboxEventBuilder(id=" + this.id + ", aggregateType=" + this.aggregateType + ", eventType=" + this.eventType + ", aggregateId=" + this.aggregateId + ", type=" + this.type + ", payload=" + this.payload + ", createdAt=" + this.createdAt + ")";
        }
    }

public static OutboxEvent.OutboxEventBuilder builder() {
        return new OutboxEvent.OutboxEventBuilder();
    }

public String getId() {
        return this.id;
    }

public com.fooddelivery.common.constants.AggregateType getAggregateType() {
        return this.aggregateType;
    }

public com.fooddelivery.common.constants.EventType getEventType() {
        return this.eventType;
    }

public String getAggregateId() {
        return this.aggregateId;
    }

public String getType() {
        return this.type;
    }

public String getPayload() {
        return this.payload;
    }

public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

public void setId(final String id) {
        this.id = id;
    }

public void setAggregateType(final com.fooddelivery.common.constants.AggregateType aggregateType) {
        this.aggregateType = aggregateType;
    }

public void setEventType(final com.fooddelivery.common.constants.EventType eventType) {
        this.eventType = eventType;
    }

public void setAggregateId(final String aggregateId) {
        this.aggregateId = aggregateId;
    }

public void setType(final String type) {
        this.type = type;
    }

public void setPayload(final String payload) {
        this.payload = payload;
    }

public void setCreatedAt(final LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @java.lang.Override
public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof OutboxEvent)) return false;
        final OutboxEvent other = (OutboxEvent) o;
        if (!other.canEqual((java.lang.Object) this)) return false;
        final java.lang.Object this$id = this.getId();
        final java.lang.Object other$id = other.getId();
        if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
        final java.lang.Object this$aggregateType = this.getAggregateType();
        final java.lang.Object other$aggregateType = other.getAggregateType();
        if (this$aggregateType == null ? other$aggregateType != null : !this$aggregateType.equals(other$aggregateType)) return false;
        final java.lang.Object this$eventType = this.getEventType();
        final java.lang.Object other$eventType = other.getEventType();
        if (this$eventType == null ? other$eventType != null : !this$eventType.equals(other$eventType)) return false;
        final java.lang.Object this$aggregateId = this.getAggregateId();
        final java.lang.Object other$aggregateId = other.getAggregateId();
        if (this$aggregateId == null ? other$aggregateId != null : !this$aggregateId.equals(other$aggregateId)) return false;
        final java.lang.Object this$type = this.getType();
        final java.lang.Object other$type = other.getType();
        if (this$type == null ? other$type != null : !this$type.equals(other$type)) return false;
        final java.lang.Object this$payload = this.getPayload();
        final java.lang.Object other$payload = other.getPayload();
        if (this$payload == null ? other$payload != null : !this$payload.equals(other$payload)) return false;
        final java.lang.Object this$createdAt = this.getCreatedAt();
        final java.lang.Object other$createdAt = other.getCreatedAt();
        if (this$createdAt == null ? other$createdAt != null : !this$createdAt.equals(other$createdAt)) return false;
        return true;
    }

protected boolean canEqual(final java.lang.Object other) {
        return other instanceof OutboxEvent;
    }

    @java.lang.Override
public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final java.lang.Object $id = this.getId();
        result = result * PRIME + ($id == null ? 43 : $id.hashCode());
        final java.lang.Object $aggregateType = this.getAggregateType();
        result = result * PRIME + ($aggregateType == null ? 43 : $aggregateType.hashCode());
        final java.lang.Object $eventType = this.getEventType();
        result = result * PRIME + ($eventType == null ? 43 : $eventType.hashCode());
        final java.lang.Object $aggregateId = this.getAggregateId();
        result = result * PRIME + ($aggregateId == null ? 43 : $aggregateId.hashCode());
        final java.lang.Object $type = this.getType();
        result = result * PRIME + ($type == null ? 43 : $type.hashCode());
        final java.lang.Object $payload = this.getPayload();
        result = result * PRIME + ($payload == null ? 43 : $payload.hashCode());
        final java.lang.Object $createdAt = this.getCreatedAt();
        result = result * PRIME + ($createdAt == null ? 43 : $createdAt.hashCode());
        return result;
    }

    @java.lang.Override
public java.lang.String toString() {
        return "OutboxEvent(id=" + this.getId() + ", aggregateType=" + this.getAggregateType() + ", eventType=" + this.getEventType() + ", aggregateId=" + this.getAggregateId() + ", type=" + this.getType() + ", payload=" + this.getPayload() + ", createdAt=" + this.getCreatedAt() + ")";
    }

public OutboxEvent() {
    }

public OutboxEvent(final String id, final com.fooddelivery.common.constants.AggregateType aggregateType, final com.fooddelivery.common.constants.EventType eventType, final String aggregateId, final String type, final String payload, final LocalDateTime createdAt) {
        this.id = id;
        this.aggregateType = aggregateType;
        this.eventType = eventType;
        this.aggregateId = aggregateId;
        this.type = type;
        this.payload = payload;
        this.createdAt = createdAt;
    }
}
