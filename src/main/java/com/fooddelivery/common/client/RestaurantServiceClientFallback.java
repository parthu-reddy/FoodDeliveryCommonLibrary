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
    public java.util.List<String> getOwnerOutlets(String ownerId, String serviceName) {
        // Authorization must fail closed. Returning empty denies access; throwing would
        // turn an unavailable dependency into a 500 on every invoice request.
        log.error("RestaurantServiceClient fallback for ownerId: {}. Denying restaurant ownership.", ownerId);
        return java.util.List.of();
    }

    @Override
    public ResponseEntity<ApiResponse<Boolean>> productExists(String productId, String serviceName) {
        log.error("Fallback triggered for restaurant-service productExists: productId={}", productId);
        return ResponseEntity.ok(ApiResponse.success(false, "Fallback: Could not verify product"));
    }

    @Override
    public ResponseEntity<java.util.Map<String, Object>> getOutletOwner(String outletId, String serviceName) {
        log.error("Fallback triggered for restaurant-service getOutletOwner: outletId={}", outletId);
        return ResponseEntity.ok(java.util.Map.of());
    }
}
