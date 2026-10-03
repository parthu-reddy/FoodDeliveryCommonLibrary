package com.fooddelivery.common.dto.organisation;

import com.fooddelivery.common.enums.*;
import java.util.UUID;
import jakarta.validation.constraints.NotNull;

public record MembershipDto(@NotNull UUID organisationId, @NotNull OrganisationStatus organisationStatus,
                            @NotNull UUID userId, @NotNull OrganisationRole role, @NotNull MembershipStatus status) { }
