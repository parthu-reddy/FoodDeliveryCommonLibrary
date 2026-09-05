package com.fooddelivery.common.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.UUID;
import com.fooddelivery.common.dto.wallet.WalletDto;
import com.fooddelivery.common.dto.wallet.CreateWalletRequest;

@FeignClient(name = "wallet-service")
public interface WalletServiceClient {
    
    @PostMapping("/api/v1/internal/wallets")
    WalletDto getOrCreateWallet(@RequestBody CreateWalletRequest request);

    @GetMapping("/api/v1/internal/wallets/{entityType}/{entityId}")
    WalletDto getWallet(@PathVariable("entityType") String entityType, @PathVariable("entityId") UUID entityId);

    @PostMapping("/api/v1/internal/wallets/{entityType}/{entityId}/debit")
    Object debit(
        @PathVariable("entityType") String entityType, 
        @PathVariable("entityId") UUID entityId, 
        @RequestBody com.fooddelivery.common.dto.wallet.TransactionRequest request,
        @org.springframework.web.bind.annotation.RequestHeader(value = "X-Service-Name", required = false) String serviceName
    );

    @GetMapping("/api/v1/internal/wallets/transactions/reference/{referenceId}")
    Object getTransactionByReference(@PathVariable("referenceId") UUID referenceId);

    @PostMapping("/api/v1/internal/advertisers/{advertiserId}/wallet/topups")
    com.fooddelivery.common.dto.ApiResponse<java.util.Map<String, String>> topupWallet(
        @PathVariable("advertiserId") UUID advertiserId,
        @RequestBody Object request,
        @org.springframework.web.bind.annotation.RequestHeader(value = "Idempotency-Key", required = true) String idempotencyKey
    );
}
