package com.fooddelivery.common.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.fooddelivery.common.dto.maps.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "mapsintegration", fallback = MapsServiceClientFallback.class)
public interface MapsServiceClient {

    @GetMapping("/api/fleet/availability/check")
    FleetAvailabilityResponseDto checkFleetAvailability(@RequestParam("cityId") String cityId, 
                                                      @RequestParam("lat") double lat, 
                                                      @RequestParam("lng") double lng, 
                                                      @RequestParam("radius") double radius);
                                               
    @GetMapping("/api/places/autocomplete")
    List<PlaceAutocompleteDto> autocomplete(@RequestParam("input") String input);
                                     
    @GetMapping("/api/places/reverse-geocode")
    PlaceGeocodeDto reverseGeocode(@RequestParam("lat") double lat, @RequestParam("lng") double lng);

    @GetMapping("/api/logistics/distance")
    DistanceResponseDto getDistance(@RequestParam("origin") String origin, @RequestParam("destination") String destination);

    @GetMapping("/api/logistics/route")
    RouteResponseDto getRoute(@RequestParam("origin") String origin, @RequestParam("destination") String destination);

    @PostMapping("/api/maps/driver/release")
    Map<String, Object> releaseDriver(@org.springframework.web.bind.annotation.RequestBody com.fooddelivery.common.dto.maps.SetAvailabilityRequest request);
}
