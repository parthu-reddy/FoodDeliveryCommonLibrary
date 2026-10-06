package com.fooddelivery.common.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;
import java.util.UUID;
import com.fooddelivery.common.dto.governmentid.GstinRequest;
import com.fooddelivery.common.dto.governmentid.BankAccountRequest;
import com.fooddelivery.common.dto.governmentid.VerificationSummary;

@FeignClient(name = "government-id-validation-service", fallbackFactory = GovernmentIdServiceClientFallback.class)
public interface GovernmentIdServiceClient {

    @GetMapping("/api/v1/internal/verification/delivery/{executiveId}/status")
    VerificationSummary getVerificationSummary(@PathVariable("executiveId") UUID executiveId);

    @GetMapping("/api/v1/verification/upload-url")
    com.fooddelivery.common.dto.governmentid.DocumentUploadDto getPresignedUploadUrl(
            @RequestParam("purpose") com.fooddelivery.common.dto.governmentid.DocumentPurpose purpose,
            @RequestParam(value = "brandId", required = false) UUID brandId,
            @RequestParam("docType") String docType, @RequestParam("contentType") String contentType,
            @RequestParam("contentLength") long contentLength);

    @PostMapping("/api/v1/verification/documents/{documentId}/complete")
    com.fooddelivery.common.dto.governmentid.ApplicationDocumentDto completeDocument(@PathVariable("documentId") UUID documentId);

    @GetMapping("/api/v1/internal/verification/delivery/{executiveId}/documents")
    java.util.List<com.fooddelivery.common.dto.governmentid.ApplicationDocumentDto> getDeliveryDocuments(@PathVariable("executiveId") UUID executiveId);

    @GetMapping("/api/v1/internal/verification/brands/{brandId}/documents")
    java.util.List<com.fooddelivery.common.dto.governmentid.ApplicationDocumentDto> getBrandDocuments(@PathVariable("brandId") UUID brandId);

    @GetMapping("/api/v1/verification/download-url")
    Map<String, String> getPresignedDownloadUrl(
            @RequestParam("objectKey") String objectKey);

    @PostMapping("/api/v1/verification/driving-license")
    com.fooddelivery.common.dto.governmentid.StatusResponseDto verifyDrivingLicense(@RequestBody com.fooddelivery.common.dto.governmentid.DLRequest request);

    @PostMapping("/api/v1/verification/vehicle-rc")
    com.fooddelivery.common.dto.governmentid.StatusResponseDto verifyVehicleRC(@RequestBody com.fooddelivery.common.dto.governmentid.RCRequest request);

    @PostMapping("/api/v1/verification/bank-account")
    com.fooddelivery.common.dto.governmentid.StatusResponseDto verifyBankAccount(@RequestBody com.fooddelivery.common.dto.governmentid.BankRequest request);

    @PostMapping("/api/v1/verification/biometric")
    com.fooddelivery.common.dto.governmentid.StatusResponseDto verifyBiometric(@RequestBody com.fooddelivery.common.dto.governmentid.BiometricRequest request);

    @PostMapping("/api/v1/verification/brands/gstin")
    void verifyGstin(@RequestBody GstinRequest request);

    @PostMapping("/api/v1/verification/brands/bank-account")
    void verifyBrandBankAccount(@RequestBody BankAccountRequest request);
}
