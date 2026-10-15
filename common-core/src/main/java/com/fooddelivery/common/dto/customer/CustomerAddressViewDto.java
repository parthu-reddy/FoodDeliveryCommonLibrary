package com.fooddelivery.common.dto.customer;

import java.util.UUID;

/**
 * A customer address as order-service reads it: the owner (each caller checks it against its own caller), the delivery
 * text written onto the order, and the coordinates used for distance, pricing and the delivery point. Served by
 * customer-service's {@code GET /api/v1/internal/customers/addresses/{addressId}} (decomposition Phase 8).
 */
public record CustomerAddressViewDto(UUID id, UUID customerId, String addressLine1, String addressLine2, String city,
                                     String state, String zipCode, Double latitude, Double longitude) {
}
