package com.fooddelivery.common.enums;

public enum OutboxStatus {
    UNPROCESSED,
    IN_PROGRESS,
    PROCESSED,
    FAILED,
    DLQ
}
