package com.fooddelivery.common.client;

import com.fooddelivery.common.dto.ApiResponse;
import com.fooddelivery.common.exception.ResourceNotFoundException;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * What RestaurantServiceClient does when a call fails. It fails fast rather than returning a default
 * value (financial integrity), and it keeps the two failures apart: the restaurant service answering
 * 404 means the outlet or brand does not exist ({@link ResourceNotFoundException}); anything else means
 * it could not be asked ({@link IllegalStateException}).
 *
 * <p>A factory, not an implementation of the client: with the Feign circuit breaker on, a plain
 * fallback swallowed the 404 too, so "no such outlet" read as "service unavailable" (BusinessPlatform
 * A2), and a component implementing the client interface can stand in for a client that was never
 * registered (W2, WalletService). Callers that fail closed on any exception behave as before.
 */
@Slf4j
@Component
public class RestaurantServiceClientFallback implements FallbackFactory<RestaurantServiceClient> {

    @Override
    public RestaurantServiceClient create(Throwable cause) {
        boolean notFound = cause instanceof FeignException.NotFound;
        return new RestaurantServiceClient() {
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
                if (notFound) {
                    throw new ResourceNotFoundException("Brand not found: " + brandId);
                }
                log.warn("Restaurant brand organisation lookup unavailable brandId={}", brandId);
                throw new IllegalStateException("Restaurant brand organisation lookup is unavailable");
            }

            @Override
            public com.fooddelivery.common.dto.restaurant.OutletOrganisationDto getOutletOrganisation(java.util.UUID outletId) {
                if (notFound) {
                    throw new ResourceNotFoundException("Outlet not found: " + outletId);
                }
                log.warn("Restaurant organisation lookup unavailable outletId={}", outletId);
                throw new IllegalStateException("Restaurant organisation lookup is unavailable");
            }
        };
    }
}
