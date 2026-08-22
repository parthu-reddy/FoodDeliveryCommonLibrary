package com.fooddelivery.common.client;

import com.fooddelivery.common.dto.wallet.WalletDto;
import com.fooddelivery.common.dto.wallet.TransactionRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.UUID;

@FeignClient(name = "wallet-service", fallback = WalletServiceClientFallback.class)
public interface WalletServiceClient {

    @GetMapping("/api/v1/wallets/{entityType}/{entityId}")
    WalletDto getWallet(@PathVariable("entityType") String entityType, @PathVariable("entityId") UUID entityId);

    @PostMapping("/api/v1/internal/wallets/{entityType}/{entityId}/credit")
    WalletDto credit(@PathVariable("entityType") String entityType, @PathVariable("entityId") UUID entityId, @RequestBody TransactionRequest request, @org.springframework.web.bind.annotation.RequestHeader("X-Calling-Service") String callingService);
    
    @PostMapping("/api/v1/internal/wallets/{entityType}/{entityId}/debit")
    WalletDto debit(@PathVariable("entityType") String entityType, @PathVariable("entityId") UUID entityId, @RequestBody TransactionRequest request, @org.springframework.web.bind.annotation.RequestHeader("X-Calling-Service") String callingService);

    @PostMapping("/api/v1/internal/wallets")
    WalletDto createWallet(@RequestBody com.fooddelivery.common.dto.wallet.CreateWalletRequest request);

    @PostMapping("/api/v1/advertisers/{advertiserId}/wallet/topups")
    com.fooddelivery.common.dto.ApiResponse<java.util.Map<String, String>> topupWallet(@PathVariable("advertiserId") UUID advertiserId, @RequestBody Object request, @org.springframework.web.bind.annotation.RequestHeader("Idempotency-Key") String idempotencyKey);
}
