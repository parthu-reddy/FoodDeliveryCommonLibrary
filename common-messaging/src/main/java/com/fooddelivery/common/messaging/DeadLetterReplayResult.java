package com.fooddelivery.common.messaging;

/** Where a dead-letter record was replayed to, and what it carried. */
public record DeadLetterReplayResult(String topic, String key, String eventType, String eventId) {
}
