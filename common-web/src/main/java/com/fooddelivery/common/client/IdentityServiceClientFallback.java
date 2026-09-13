package com.fooddelivery.common.client;

import com.fooddelivery.common.dto.ApiResponse;
import com.fooddelivery.common.dto.identity.IdentityUserDTO;
import com.fooddelivery.common.dto.identity.RoleRequestDTO;
import org.springframework.stereotype.Component;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

@Component("commonlibraryIdentityServiceClientFallback")
@lombok.RequiredArgsConstructor
public class IdentityServiceClientFallback implements IdentityServiceClient {
    @Override
    public ResponseEntity<ApiResponse<String>> initiateLogin(String phoneNumber, String serviceName) {
        throw new IllegalStateException("Identity service is currently unavailable.");
    }

    @Override
    public ResponseEntity<ApiResponse<String>> verifyOtp(String phoneNumber, String otp, String serviceName) {
        throw new IllegalStateException("Identity service is currently unavailable.");
    }

    @Override
    public ResponseEntity<ApiResponse<IdentityUserDTO>> getUserById(UUID id, String serviceName) {
        throw new IllegalStateException("Identity service is currently unavailable.");
    }



}
