package com.fooddelivery.common.dto.restaurant;

import com.fooddelivery.common.enums.ApplicationStatus;

import java.util.UUID;

/**
 * Immutable outlet routing data. Membership is checked separately on every authorization.
 *
 * <p>The brand's application status, whether the outlet is active, its fleet city and its name let a
 * campaign check that the outlet it promotes is the organisation's own, approved and serving, and
 * target the outlet's city (BusinessPlatform A2). Its banner is what an OUTLET_BANNER ad creative shows (A3).
 */
public record OutletOrganisationDto(UUID outletId, UUID brandId, UUID organisationId,
                                    ApplicationStatus brandApplicationStatus, Boolean active,
                                    String cityId, String name, String bannerUrl) { }
