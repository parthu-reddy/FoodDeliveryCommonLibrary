package com.fooddelivery.common.event.organisation;

import com.fooddelivery.common.enums.*;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record OrganisationMembershipChangedEvent(
        @NotNull UUID organisationId,
        @NotNull UUID userId,
        OrganisationRole role,
        @NotNull MembershipStatus status,
        @NotNull UUID changedBy,
        @NotNull Instant changedAt) {
    public OrganisationMembershipChangedEvent {
        if ((status == MembershipStatus.ACTIVE && role == null)
                || (status == MembershipStatus.REMOVED && role != null)) {
            throw new IllegalArgumentException(
                    "Active membership needs a role; removed membership has no role");
        }
    }
}
