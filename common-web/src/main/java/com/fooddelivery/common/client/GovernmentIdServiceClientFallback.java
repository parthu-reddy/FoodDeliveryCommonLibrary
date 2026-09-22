package com.fooddelivery.common.client;

import feign.FeignException;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.Map;
import java.util.UUID;
import com.fooddelivery.common.dto.governmentid.GstinRequest;
import com.fooddelivery.common.dto.governmentid.BankAccountRequest;

@Component
@lombok.extern.slf4j.Slf4j
public class GovernmentIdServiceClientFallback implements FallbackFactory<GovernmentIdServiceClient> {

    @Override
    public GovernmentIdServiceClient create(Throwable cause) {
        return new GovernmentIdServiceClient() {

            private <T> T handleException(String method) {
                if (cause instanceof FeignException) {
                    FeignException fe = (FeignException) cause;
                    if (fe.status() == 429) {
                        log.warn("Rate limit exceeded in GovernmentId service for {}", method);
                        throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Rate limit exceeded. Please try again later.", cause);
                    }
                }
                log.error("GovernmentId service is down. Fallback triggered for {}", method, cause);
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "GovernmentId service is currently unavailable", cause);
            }

            @Override
            public VerificationSummary getVerificationSummary(UUID executiveId) {
                return handleException("getVerificationSummary");
            }

            @Override
            public Map<String, String> getPresignedUploadUrl(String docType, String contentType) {
                return handleException("getPresignedUploadUrl");
            }

            @Override
            public Map<String, String> getPresignedDownloadUrl(String objectKey) {
                return handleException("getPresignedDownloadUrl");
            }

            @Override
            public com.fooddelivery.common.dto.governmentid.StatusResponseDto verifyDrivingLicense(com.fooddelivery.common.dto.governmentid.DLRequest request) {
                return handleException("verifyDrivingLicense");
            }

            @Override
            public com.fooddelivery.common.dto.governmentid.StatusResponseDto verifyVehicleRC(com.fooddelivery.common.dto.governmentid.RCRequest request) {
                return handleException("verifyVehicleRC");
            }

            @Override
            public com.fooddelivery.common.dto.governmentid.StatusResponseDto verifyBankAccount(com.fooddelivery.common.dto.governmentid.BankRequest request) {
                return handleException("verifyBankAccount");
            }

            @Override
            public com.fooddelivery.common.dto.governmentid.StatusResponseDto verifyBiometric(com.fooddelivery.common.dto.governmentid.BiometricRequest request) {
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
