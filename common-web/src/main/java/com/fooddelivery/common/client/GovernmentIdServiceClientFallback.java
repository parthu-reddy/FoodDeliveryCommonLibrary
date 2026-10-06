package com.fooddelivery.common.client;

import com.fooddelivery.common.dto.governmentid.BankAccountRequest;
import com.fooddelivery.common.dto.governmentid.GstinRequest;

import feign.FeignException;

import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.UUID;

@Component
@lombok.extern.slf4j.Slf4j
public class GovernmentIdServiceClientFallback
        implements FallbackFactory<GovernmentIdServiceClient> {

    @Override
    public GovernmentIdServiceClient create(Throwable cause) {
        return new GovernmentIdServiceClient() {

            private <T> T handleException(String method) {
                Throwable current = cause;
                java.util.Set<Throwable> seen =
                        java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
                while (current != null && seen.add(current)) {
                    if (current instanceof FeignException failure
                            && failure.status() >= 400
                            && failure.status() < 500) {
                        throw new ResponseStatusException(
                                HttpStatus.valueOf(failure.status()),
                                failure.status() == 429
                                        ? "Too many verification requests. Please try again later."
                                        : "Verification request was refused. Check your application"
                                              + " details.");
                    }
                    current = current.getCause();
                }
                log.warn("GovernmentId service unavailable method={}", method);
                throw new ResponseStatusException(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        "GovernmentId service is currently unavailable");
            }

            @Override
            public VerificationSummary getVerificationSummary(UUID executiveId) {
                return handleException("getVerificationSummary");
            }

            @Override
            public com.fooddelivery.common.dto.governmentid.DocumentUploadDto getPresignedUploadUrl(
                    com.fooddelivery.common.dto.governmentid.DocumentPurpose purpose,
                    UUID brandId,
                    String docType,
                    String contentType,
                    long contentLength) {
                return handleException("getPresignedUploadUrl");
            }

            @Override
            public com.fooddelivery.common.dto.governmentid.ApplicationDocumentDto completeDocument(
                    UUID documentId) {
                return handleException("completeDocument");
            }

            @Override
            public java.util.List<com.fooddelivery.common.dto.governmentid.ApplicationDocumentDto>
                    getDeliveryDocuments(UUID executiveId) {
                return handleException("getDeliveryDocuments");
            }

            @Override
            public java.util.List<com.fooddelivery.common.dto.governmentid.ApplicationDocumentDto>
                    getBrandDocuments(UUID brandId) {
                return handleException("getBrandDocuments");
            }

            @Override
            public Map<String, String> getPresignedDownloadUrl(String objectKey) {
                return handleException("getPresignedDownloadUrl");
            }

            @Override
            public com.fooddelivery.common.dto.governmentid.StatusResponseDto verifyDrivingLicense(
                    com.fooddelivery.common.dto.governmentid.DLRequest request) {
                return handleException("verifyDrivingLicense");
            }

            @Override
            public com.fooddelivery.common.dto.governmentid.StatusResponseDto verifyVehicleRC(
                    com.fooddelivery.common.dto.governmentid.RCRequest request) {
                return handleException("verifyVehicleRC");
            }

            @Override
            public com.fooddelivery.common.dto.governmentid.StatusResponseDto verifyBankAccount(
                    com.fooddelivery.common.dto.governmentid.BankRequest request) {
                return handleException("verifyBankAccount");
            }

            @Override
            public com.fooddelivery.common.dto.governmentid.StatusResponseDto verifyBiometric(
                    com.fooddelivery.common.dto.governmentid.BiometricRequest request) {
                return handleException("verifyBiometric");
            }

            @Override
            public void verifyGstin(GstinRequest request) {
                handleException("verifyGstin");
            }

            @Override
            public void verifyBrandBankAccount(BankAccountRequest request) {
                handleException("verifyBankAccount");
            }
        };
    }
}
