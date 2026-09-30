package com.fooddelivery.common.location;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Resolves a city requested by an administrative fleet endpoint against the deployment's allowed
 * operating areas.
 *
 * <p>The configuration is a comma-separated list so the same value can be supplied through a
 * regular environment variable. With exactly one configured city, creation endpoints may omit a
 * city id and are assigned that single configured scope. Once more than one city is configured an
 * explicit id is required, preventing a new address or outlet from silently being assigned to an
 * arbitrary city.
 */
public final class FleetCityScope {

    private FleetCityScope() {
    }

    public static List<String> parseAllowed(String configuredCityIds) {
        if (configuredCityIds == null || configuredCityIds.isBlank()) {
            throw new IllegalStateException("platform.fleet.allowed-city-ids must contain at least one city id");
        }

        Set<String> cityIds = new LinkedHashSet<>();
        Arrays.stream(configuredCityIds.split(",", -1))
                .map(String::trim)
                .forEach(cityId -> cityIds.add(CityIdValidator.requireCanonical(cityId)));
        return List.copyOf(cityIds);
    }

    /** Resolves an optional input and rejects cities outside the configured operating scope. */
    public static String resolve(String requestedCityId, String configuredCityIds) {
        List<String> allowed = parseAllowed(configuredCityIds);
        if (requestedCityId == null) {
            if (allowed.size() == 1) {
                return allowed.get(0);
            }
            throw new IllegalArgumentException("cityId is required when more than one fleet city is configured");
        }

        String cityId = CityIdValidator.requireCanonical(requestedCityId);
        if (!allowed.contains(cityId)) {
            throw new IllegalArgumentException("cityId is not an enabled fleet city");
        }
        return cityId;
    }
}
