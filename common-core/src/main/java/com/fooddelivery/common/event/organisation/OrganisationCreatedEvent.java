package com.fooddelivery.common.event.organisation;

import java.time.Instant;
import java.util.UUID;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

public record OrganisationCreatedEvent(@NotNull UUID organisationId, @NotBlank String displayName,
                                       @NotNull UUID createdBy, @NotNull Instant createdAt) { }
