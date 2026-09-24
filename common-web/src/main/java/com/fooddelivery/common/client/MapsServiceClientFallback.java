package com.fooddelivery.common.client;

import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import com.fooddelivery.common.dto.maps.*;

@Component
@Slf4j
@lombok.RequiredArgsConstructor
public class MapsServiceClientFallback implements MapsServiceClient {

    @Override
    public FleetAvailabilityResponseDto checkFleetAvailability(String cityId, double lat, double lng, double radius) {
        log.error("Maps service is unavailable for fleet check cityId={} radiusKm={}; refusing fail-open availability",
                cityId, radius);
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                "Fleet availability is temporarily unavailable");
    }

    @Override
    public List<PlaceAutocompleteDto> autocomplete(String input) {
        return new ArrayList<>();
    }

    @Override
    public PlaceGeocodeDto reverseGeocode(double lat, double lng) {
        return PlaceGeocodeDto.builder()
                .formattedAddress("Current Location (Offline Geocoder)")
                .lat(lat)
                .lng(lng)
                .build();
    }

    @Override
    public DistanceResponseDto getDistance(String origin, String destination) {
        throw new IllegalStateException("Maps routing service unavailable. Cannot compute delivery distance. No fallback defaults permitted for financial integrity.");
    }

    @Override
    public com.fooddelivery.common.dto.ApiResponse<RouteResponseDto> getRoute(String origin, String destination) {
        log.error("Maps service is down. Fallback triggered for getRoute");
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Maps service is currently unavailable");
    }

    @Override
    public java.util.Map<String, Object> releaseDriver(com.fooddelivery.common.dto.maps.SetAvailabilityRequest request) {
        log.error("Maps service is down. Fallback triggered for releaseDriver");
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Maps service is currently unavailable");
    }

    @Override
    public java.util.Map<String, Object> setDriverAvailability(com.fooddelivery.common.dto.maps.SetAvailabilityRequest request) {
        log.error("Maps service is down. Fallback triggered for setDriverAvailability");
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Maps service is currently unavailable");
    }
}
