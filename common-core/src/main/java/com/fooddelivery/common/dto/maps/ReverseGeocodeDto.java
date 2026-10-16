package com.fooddelivery.common.dto.maps;

/**
 * MapsIntegration's answer to {@code GET /api/places/reverse-geocode}: {@code {"address": "..."}} (its
 * ReverseGeocodeResponse and contract). Decoding that body into {@link PlaceGeocodeDto} left every field null
 * (ServiceDecomposition F-9); callers map it into their own response.
 */
public record ReverseGeocodeDto(String address) {
}
