package com.fooddelivery.common.messaging;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Identifies one record on a dead-letter topic: {@code <topic>-dlt} for {@code @RetryableTopic}
 * listeners, {@code <topic>.DLT} for those handled by the shared DefaultErrorHandler.
 */
public record DeadLetterReplayRequest(
        @NotBlank String dltTopic,
        @NotNull @PositiveOrZero Integer partition,
        @NotNull @PositiveOrZero Long offset
) {
}
