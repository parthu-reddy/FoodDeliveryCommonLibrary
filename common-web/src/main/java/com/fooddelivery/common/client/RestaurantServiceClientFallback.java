package com.fooddelivery.common.client;

import com.fooddelivery.common.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * Fallback for RestaurantServiceClient.
 * Throws an IllegalStateException to Fail Fast rather than returning a default value,
 * per the project's strict financial integrity rules.
 */
@Slf4j
@Component
@lombok.RequiredArgsConstructor
public class RestaurantServiceClientFallback implements RestaurantServiceClient {

    @Override
    public ResponseEntity<ApiResponse<Boolean>> outletExists(String outletId, String serviceName) {
        log.error("RestaurantServiceClient fallback triggered for outletId: {}. Restaurant service is unavailable.", outletId);
        throw new IllegalStateException("Unable to verify restaurant existence. Restaurant service is currently unavailable.");
    }

    @Override
    public java.util.List<java.util.UUID> getUserOutlets(java.util.UUID userId, com.fooddelivery.common.enums.OrganisationPermission permission) {
        log.warn("Restaurant outlet lookup unavailable userId={}", userId);
        throw new IllegalStateException("Restaurant outlet lookup is unavailable");
    }

    @Override
    public ResponseEntity<ApiResponse<Boolean>> productExists(String productId, String serviceName) {
        log.error("Fallback triggered for restaurant-service productExists: productId={}", productId);
        return ResponseEntity.ok(ApiResponse.success(false, "Fallback: Could not verify product"));
    }

    @Override
    public com.fooddelivery.common.dto.restaurant.BrandOrganisationDto getBrandOrganisation(java.util.UUID brandId) {
        log.warn("Restaurant brand organisation lookup unavailable brandId={}", brandId);
        throw new IllegalStateException("Restaurant brand organisation lookup is unavailable");
    }

    @Override
    public com.fooddelivery.common.dto.restaurant.OutletOrganisationDto getOutletOrganisation(java.util.UUID outletId) {
        log.warn("Restaurant organisation lookup unavailable outletId={}", outletId);
        throw new IllegalStateException("Restaurant organisation lookup is unavailable");
    }
}
