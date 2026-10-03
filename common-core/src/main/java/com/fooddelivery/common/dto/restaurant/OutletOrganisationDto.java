package com.fooddelivery.common.dto.restaurant;

import java.util.UUID;

/** Immutable outlet routing data. Membership is checked separately on every authorization. */
public record OutletOrganisationDto(UUID outletId, UUID brandId, UUID organisationId) { }
