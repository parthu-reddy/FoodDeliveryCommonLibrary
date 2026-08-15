package com.fooddelivery.common.client;

import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.Map;
import java.util.UUID;
import com.fooddelivery.common.dto.governmentid.GstinRequest;
import com.fooddelivery.common.dto.governmentid.BankAccountRequest;

@Component
@lombok.extern.slf4j.Slf4j
public class GovernmentIdServiceClientFallback implements GovernmentIdServiceClient {

    @Override
    public VerificationSummary getVerificationSummary(UUID executiveId) {
        log.error("GovernmentId service is down. Fallback triggered for getVerificationSummary");
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "GovernmentId service is currently unavailable");
    }

    @Override
    public Map<String, String> getPresignedUploadUrl(String docType, String contentType) {
        log.error("GovernmentId service is down. Fallback triggered for getPresignedUploadUrl");
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "GovernmentId service is currently unavailable");
    }

    @Override
    public Map<String, String> getPresignedDownloadUrl(String objectKey) {
        log.error("GovernmentId service is down. Fallback triggered for getPresignedDownloadUrl");
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "GovernmentId service is currently unavailable");
    }

    @Override
    public Object verifyDrivingLicense(com.fooddelivery.common.dto.governmentid.DLRequest request) {
        log.error("GovernmentId service is down. Fallback triggered for verifyDrivingLicense");
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "GovernmentId service is currently unavailable");
    }

    @Override
    public Object verifyVehicleRC(com.fooddelivery.common.dto.governmentid.RCRequest request) {
        log.error("GovernmentId service is down. Fallback triggered for verifyVehicleRC");
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "GovernmentId service is currently unavailable");
    }

    @Override
    public Object verifyBankAccount(com.fooddelivery.common.dto.governmentid.BankRequest request) {
        log.error("GovernmentId service is down. Fallback triggered for verifyBankAccount");
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "GovernmentId service is currently unavailable");
    }

    @Override
    public Object verifyBiometric(com.fooddelivery.common.dto.governmentid.BiometricRequest request) {
        log.error("GovernmentId service is down. Fallback triggered for verifyBiometric");
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "GovernmentId service is currently unavailable");
    }

    @Override
    public void verifyGstin(GstinRequest request) {
        log.error("GovernmentId service is down. Fallback triggered for verifyGstin");
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "GovernmentId service is currently unavailable");
    }

    @Override
    public void verifyBrandBankAccount(BankAccountRequest request) {
        log.error("GovernmentId service is down. Fallback triggered for verifyBankAccount");
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "GovernmentId service is currently unavailable");
    }
}
