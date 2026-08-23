package com.fooddelivery.common.client;

import com.fooddelivery.common.dto.wallet.WalletDto;
import com.fooddelivery.common.dto.wallet.TransactionRequest;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
@lombok.extern.slf4j.Slf4j
@lombok.RequiredArgsConstructor
public class WalletServiceClientFallback implements WalletServiceClient {

    @Override
    public WalletDto getWallet(String entityType, UUID entityId) {
        log.error("Wallet service is down. Fallback triggered for getWallet for {} {}", entityType, entityId);
        throw new RuntimeException("Wallet service is currently unavailable");
    }

    @Override
    public WalletDto credit(String entityType, UUID entityId, TransactionRequest request, String callingService) {
        log.error("Wallet service unavailable for credit: {}/{}", entityType, entityId);
        throw new IllegalStateException("Wallet service is currently unavailable");
    }

    @Override
    public WalletDto debit(String entityType, UUID entityId, TransactionRequest request, String callingService) {
        log.error("Wallet service unavailable for debit: {}/{}", entityType, entityId);
        throw new IllegalStateException("Wallet service is currently unavailable");
    }

    @Override
    public WalletDto createWallet(com.fooddelivery.common.dto.wallet.CreateWalletRequest request) {
        log.error("Wallet service unavailable for createWallet");
        throw new RuntimeException("Wallet service is currently unavailable");
    }

    @Override
    public com.fooddelivery.common.dto.ApiResponse<java.util.Map<String, String>> topupWallet(UUID advertiserId, Object request, String idempotencyKey) {
        log.error("Wallet service unavailable for topupWallet: {}", advertiserId);
        throw new IllegalStateException("Wallet service is currently unavailable");
    }
}
