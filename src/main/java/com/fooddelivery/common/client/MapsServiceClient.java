package com.fooddelivery.common.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(name = "mapsintegration", fallback = MapsServiceClientFallback.class)
public interface MapsServiceClient {

    @GetMapping("/api/fleet/availability/check")
    Map<String, Object> checkFleetAvailability(@RequestParam("cityId") String cityId, 
                                               @RequestParam("lat") double lat, 
                                               @RequestParam("lng") double lng, 
                                               @RequestParam("radius") double radius);
                                               
    @GetMapping("/api/places/autocomplete")
    List<Map<String, Object>> autocomplete(@RequestParam("input") String input);
                                     
    @GetMapping("/api/places/reverse-geocode")
    Map<String, Object> reverseGeocode(@RequestParam("lat") double lat, @RequestParam("lng") double lng);

    @GetMapping("/api/logistics/distance")
    Map<String, Object> getDistance(@RequestParam("origin") String origin, @RequestParam("destination") String destination);

    @GetMapping("/api/logistics/route")
    Map<String, Object> getRoute(@RequestParam("origin") String origin, @RequestParam("destination") String destination);

    @org.springframework.web.bind.annotation.PostMapping("/api/fleet/release")
    Map<String, Object> releaseDriver(@org.springframework.web.bind.annotation.RequestBody com.fooddelivery.common.dto.maps.SetAvailabilityRequest request);
}
