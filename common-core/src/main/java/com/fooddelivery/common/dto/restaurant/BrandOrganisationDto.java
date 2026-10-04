package com.fooddelivery.common.dto.restaurant;

import com.fooddelivery.common.enums.ApplicationStatus;
import java.util.UUID;

/** Minimal internal resolution for document-purpose authorization. */
public record BrandOrganisationDto(UUID brandId, UUID organisationId, ApplicationStatus applicationStatus) { }
