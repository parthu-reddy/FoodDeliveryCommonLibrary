package com.fooddelivery.common.event;

import com.fooddelivery.common.enums.KitchenRole;
import com.fooddelivery.common.enums.KitchenStaffStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * A kitchen staff row changed (kitchen-events, key = staffId). Identity projects it into the KITCHEN role and portal.
 *
 * <p>Carries the ten-digit phone because the person may not have an account yet (D-4): identity matches it when they
 * sign in. Consumers must never log it. {@code staffVersion} is monotonic per staffId, so a late or replayed event
 * cannot undo a newer one. A REMOVED event still carries the last roles.
 */
@lombok.Data
@lombok.Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
public class KitchenStaffChangedEvent {
    @NotNull(message = "staffId is required")
    private UUID staffId;
    @NotNull(message = "organisationId is required")
    private UUID organisationId;
    @NotNull(message = "outletId is required")
    private UUID outletId;
    @NotBlank(message = "phoneNumber is required")
    @Pattern(regexp = "\\d{10}", message = "phoneNumber must be ten digits")
    private String phoneNumber;
    /** Null while the row is INVITED (nobody has signed in with the phone yet). */
    private UUID userId;
    @NotEmpty(message = "roles are required")
    private Set<KitchenRole> roles;
    @NotNull(message = "status is required")
    private KitchenStaffStatus status;
    @PositiveOrZero(message = "staffVersion must not be negative")
    private long staffVersion;
    @NotNull(message = "occurredAt is required")
    private Instant occurredAt;
}
