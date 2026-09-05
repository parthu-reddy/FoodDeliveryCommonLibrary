package com.fooddelivery.common.client;

import com.fooddelivery.common.dto.wallet.WalletDto;
import com.fooddelivery.common.dto.wallet.TransactionRequest;
import com.fooddelivery.common.enums.WalletEntityType;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import java.util.UUID;
import java.util.List;
import java.util.Map;

@FeignClient(name = "wallet-service", fallback = WalletInternalClientFallback.class)
public interface WalletInternalClient {

    @GetMapping("/api/v1/internal/wallets/{entityType}/{entityId}")
    WalletDto getWallet(@PathVariable("entityType") WalletEntityType entityType, @PathVariable("entityId") UUID entityId);

    @PostMapping("/api/v1/internal/wallets/{entityType}/{entityId}/get-or-create")
    WalletDto getOrCreate(@PathVariable("entityType") WalletEntityType entityType, @PathVariable("entityId") UUID entityId, @RequestHeader("X-Calling-Service") String callingService);

    @PostMapping("/api/v1/internal/wallets/{entityType}/{entityId}/credit")
    WalletDto credit(@PathVariable("entityType") WalletEntityType entityType, @PathVariable("entityId") UUID entityId, @RequestBody TransactionRequest request, @RequestHeader("X-Calling-Service") String callingService);
    
    @PostMapping("/api/v1/internal/wallets/{entityType}/{entityId}/debit")
    WalletDto debit(@PathVariable("entityType") WalletEntityType entityType, @PathVariable("entityId") UUID entityId, @RequestBody TransactionRequest request, @RequestHeader("X-Calling-Service") String callingService);

    @GetMapping("/api/v1/internal/wallets/{entityType}/{entityId}/transactions/{reference}")
    List<Map<String, Object>> getTransactions(@PathVariable("entityType") WalletEntityType entityType, @PathVariable("entityId") UUID entityId, @PathVariable("reference") UUID reference);
}
