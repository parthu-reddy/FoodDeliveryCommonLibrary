package com.fooddelivery.common.event;

import java.time.LocalDateTime;

@lombok.Data
@lombok.Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public class OutboxEvent {
    private String id;
    private com.fooddelivery.common.constants.AggregateType aggregateType;
    private com.fooddelivery.common.constants.EventType eventType;
    private String aggregateId;
    private String type;
    private String payload;
    private LocalDateTime createdAt;






















}
