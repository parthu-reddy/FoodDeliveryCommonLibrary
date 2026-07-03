package com.fooddelivery.common.client;

import com.fooddelivery.common.dto.ApiResponse;
import com.fooddelivery.common.dto.identity.IdentityUserDTO;
import com.fooddelivery.common.dto.identity.RoleRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "identity-service", url = "${identity.service.url:http://localhost:8085}")
public interface IdentityServiceClient {

    @PostMapping("/api/v1/internal/auth/initiate")
    ResponseEntity<ApiResponse<String>> initiateLogin(
            @RequestParam("phoneNumber") String phoneNumber,
            @RequestHeader("X-Calling-Service") String serviceName);

    @PostMapping("/api/v1/internal/auth/verify")
    ResponseEntity<ApiResponse<String>> verifyOtp(
            @RequestParam("phoneNumber") String phoneNumber,
            @RequestParam("otp") String otp,
            @RequestHeader("X-Calling-Service") String serviceName);

    @GetMapping("/api/v1/internal/users/{id}")
    ResponseEntity<ApiResponse<IdentityUserDTO>> getUserById(
            @PathVariable("id") UUID id,
            @RequestHeader("X-Calling-Service") String serviceName);

    @GetMapping("/api/v1/internal/users/by-role")
    ResponseEntity<ApiResponse<List<IdentityUserDTO>>> getUsersByRole(
            @RequestParam("role") String role,
            @RequestHeader("X-Calling-Service") String serviceName);

    @PostMapping("/api/v1/internal/users/{id}/roles")
    ResponseEntity<ApiResponse<Void>> assignRole(
            @PathVariable("id") UUID id,
            @RequestBody RoleRequestDTO roleRequest,
            @RequestHeader("X-Calling-Service") String callingService);
}
