package com.fooddelivery.common.client;

import com.fooddelivery.common.dto.ApiResponse;
import com.fooddelivery.common.dto.identity.IdentityUserDTO;
import com.fooddelivery.common.dto.identity.RoleRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "identity-service", fallback = IdentityServiceClientFallback.class)
public interface IdentityServiceClient {

    // getUsersByRole, assignRole and removeRole were declared here and called by nobody. The
    // endpoints behind them moved to /api/v1/internal/admin/users/** on 2026-09-09 when the admin
    // user surface was split from the service-to-service one; rather than re-point declarations
    // with no callers, they were deleted. getUserById below is the only method with a caller.

    @PostMapping("/api/v1/internal/auth/initiate")
    ResponseEntity<ApiResponse<String>> initiateLogin(
            @RequestParam("phoneNumber") String phoneNumber,
            @RequestHeader(com.fooddelivery.common.constants.HeaderConstants.HEADER_CALLING_SERVICE) String serviceName);

    @PostMapping("/api/v1/internal/auth/verify")
    ResponseEntity<ApiResponse<String>> verifyOtp(
            @RequestParam("phoneNumber") String phoneNumber,
            @RequestParam("otp") String otp,
            @RequestHeader(com.fooddelivery.common.constants.HeaderConstants.HEADER_CALLING_SERVICE) String serviceName);

    @GetMapping("/api/v1/internal/admin/users/{id}")
    ResponseEntity<ApiResponse<IdentityUserDTO>> getUserById(
            @PathVariable("id") UUID id,
            @RequestHeader(com.fooddelivery.common.constants.HeaderConstants.HEADER_CALLING_SERVICE) String serviceName);
}
