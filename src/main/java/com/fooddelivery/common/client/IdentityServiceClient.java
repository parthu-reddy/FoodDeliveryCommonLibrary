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

    @PostMapping("/api/v1/internal/auth/initiate")
    ResponseEntity<ApiResponse<String>> initiateLogin(
            @RequestParam("phoneNumber") String phoneNumber,
            @RequestHeader(com.fooddelivery.common.constants.HeaderConstants.HEADER_CALLING_SERVICE) String serviceName);

    @PostMapping("/api/v1/internal/auth/verify")
    ResponseEntity<ApiResponse<String>> verifyOtp(
            @RequestParam("phoneNumber") String phoneNumber,
            @RequestParam("otp") String otp,
            @RequestHeader(com.fooddelivery.common.constants.HeaderConstants.HEADER_CALLING_SERVICE) String serviceName);

    @GetMapping("/api/v1/internal/users/{id}")
    ResponseEntity<ApiResponse<IdentityUserDTO>> getUserById(
            @PathVariable("id") UUID id,
            @RequestHeader(com.fooddelivery.common.constants.HeaderConstants.HEADER_CALLING_SERVICE) String serviceName);

    @GetMapping("/api/v1/internal/users/by-role")
    ResponseEntity<ApiResponse<List<IdentityUserDTO>>> getUsersByRole(
            @RequestParam("role") String role,
            @RequestHeader(com.fooddelivery.common.constants.HeaderConstants.HEADER_CALLING_SERVICE) String serviceName);

    @PostMapping("/api/v1/internal/users/{id}/roles")
    ResponseEntity<ApiResponse<String>> assignRole(
            @PathVariable("id") UUID id,
            @RequestBody RoleRequestDTO roleRequest,
            @RequestHeader(com.fooddelivery.common.constants.HeaderConstants.HEADER_CALLING_SERVICE) String callingService);

    @DeleteMapping("/api/v1/internal/users/{id}/roles/{roleName}")
    ResponseEntity<ApiResponse<String>> removeRole(
            @PathVariable("id") UUID id,
            @PathVariable("roleName") String roleName,
            @RequestHeader(com.fooddelivery.common.constants.HeaderConstants.HEADER_CALLING_SERVICE) String callingService);
}
