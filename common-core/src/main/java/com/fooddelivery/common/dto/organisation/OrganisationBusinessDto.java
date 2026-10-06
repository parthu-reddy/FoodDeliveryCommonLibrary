package com.fooddelivery.common.dto.organisation;

import com.fooddelivery.common.enums.*;
import java.util.UUID;
import jakarta.validation.constraints.NotNull;

/** One business an organisation has applied for, with its current application status. */
public record OrganisationBusinessDto(@NotNull BusinessType businessType, @NotNull UUID businessId,
                                      @NotNull ApplicationStatus applicationStatus) { }
