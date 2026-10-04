package com.fooddelivery.common.event.application;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fooddelivery.common.enums.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DeliveryApplicationStatusChangedEvent(
        @NotNull UUID executiveId,
        @NotNull ApplicationStatus status,
        @Size(max = 500) String reason,
        @JsonProperty(value = "applicationVersion", required = true) @PositiveOrZero long applicationVersion,
        @NotNull Instant changedAt) { }
