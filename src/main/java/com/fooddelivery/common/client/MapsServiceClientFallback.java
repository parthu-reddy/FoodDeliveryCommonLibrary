package com.fooddelivery.common.client;

import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class MapsServiceClientFallback implements MapsServiceClient {

    @Override
    public Map<String, Object> checkFleetAvailability(String cityId, double lat, double lng, double radius) {
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("available", true);
        fallback.put("count", 3);
        fallback.put("fallback", true);
        return fallback;
    }

    @Override
    public List<Map<String, Object>> autocomplete(String input) {
        return new ArrayList<>();
    }

    @Override
    public Map<String, Object> reverseGeocode(double lat, double lng) {
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("formatted_address", "Current Location (Offline Geocoder)");
        fallback.put("lat", lat);
        fallback.put("lng", lng);
        fallback.put("fallback", true);
        return fallback;
    }

    @Override
    public Map<String, Object> getDistance(String origin, String destination) {
        throw new IllegalStateException("Maps routing service unavailable. Cannot compute delivery distance. No fallback defaults permitted for financial integrity.");
    }

    @Override
    public Map<String, Object> getRoute(String origin, String destination) {
        log.error("Maps service is down. Fallback triggered for getRoute");
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Maps service is currently unavailable");
    }

    @Override
    public Map<String, Object> releaseDriver(com.fooddelivery.common.dto.maps.SetAvailabilityRequest request) {
        log.error("Maps service is down. Fallback triggered for releaseDriver");
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Maps service is currently unavailable");
    }
}
