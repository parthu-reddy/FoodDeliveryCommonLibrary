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
public class RestaurantServiceClientFallback implements RestaurantServiceClient {

    @Override
    public ResponseEntity<ApiResponse<Boolean>> outletExists(String outletId, String serviceName) {
        log.error("RestaurantServiceClient fallback triggered for outletId: {}. Restaurant service is unavailable.", outletId);
        throw new IllegalStateException("Unable to verify restaurant existence. Restaurant service is currently unavailable.");
    }

    @Override
    public ResponseEntity<ApiResponse<Boolean>> productExists(String productId, String serviceName) {
        log.error("RestaurantServiceClient fallback triggered for productId: {}. Restaurant service is unavailable.", productId);
        throw new IllegalStateException("Unable to verify product existence. Restaurant service is currently unavailable.");
    }
}
