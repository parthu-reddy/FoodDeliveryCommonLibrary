package com.fooddelivery.common.event.organisation;

import com.fooddelivery.common.enums.OrganisationStatus;
import java.time.Instant;
import java.util.UUID;
import jakarta.validation.constraints.NotNull;

public record OrganisationStatusChangedEvent(@NotNull UUID organisationId, @NotNull OrganisationStatus status,
                                            String reason, @NotNull Instant changedAt) { }
