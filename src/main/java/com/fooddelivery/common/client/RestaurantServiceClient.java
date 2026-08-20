package com.fooddelivery.common.client;

import com.fooddelivery.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * Feign client for validating that a restaurant (outlet) exists before accepting a review.
 * Calls the RestaurantApplication's internal API.
 */
@FeignClient(name = "restaurant-service", fallback = RestaurantServiceClientFallback.class)
public interface RestaurantServiceClient {

    @GetMapping("/api/v1/internal/restaurants/outlets/{outletId}/exists")
    ResponseEntity<ApiResponse<Boolean>> outletExists(
            @PathVariable("outletId") String outletId,
            @RequestHeader(com.fooddelivery.common.constants.HeaderConstants.HEADER_CALLING_SERVICE) String serviceName);

    @GetMapping("/api/v1/internal/restaurants/products/{productId}/exists")
    ResponseEntity<ApiResponse<Boolean>> productExists(
            @PathVariable("productId") String productId,
            @RequestHeader(com.fooddelivery.common.constants.HeaderConstants.HEADER_CALLING_SERVICE) String serviceName);
}
