package com.fooddelivery.common.dto.restaurant;

import com.fooddelivery.common.enums.ApplicationStatus;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

/**
 * What catalog-service needs to know about an outlet restaurant-service owns: its brand, the brand's approval, the
 * outlet's IANA {@code timeZone} and its daily opening windows. Served by restaurant-service's
 * {@code GET /api/v1/internal/restaurants/outlets/{outletId}/catalog-context}; {@code applicationStatus} is null when
 * the brand row is missing, which the catalog treats like any unapproved brand.
 */
public record OutletCatalogContextDto(UUID outletId, UUID brandId, ApplicationStatus applicationStatus, String timeZone,
                                      List<HoursWindow> hours) {

    /** One daily opening window, {@code [opens, closes)} on the outlet's wall clock (see OpeningHours). */
    public record HoursWindow(LocalTime opens, LocalTime closes) { }
}
