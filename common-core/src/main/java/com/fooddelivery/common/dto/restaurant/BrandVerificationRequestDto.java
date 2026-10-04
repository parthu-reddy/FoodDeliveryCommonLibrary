package com.fooddelivery.common.dto.restaurant;

import java.util.UUID;

/** SERVICE-only provider input; financial data is never put into the application status event. */
public record BrandVerificationRequestDto(UUID brandId, UUID organisationId, long applicationVersion,
        boolean eligible, String brandName, String gstin, String bankAccountNumber, String ifscCode) {
    @Override public String toString() {
        return "BrandVerificationRequestDto[brandId=" + brandId + ",applicationVersion=" + applicationVersion + ",eligible=" + eligible + "]";
    }
}
