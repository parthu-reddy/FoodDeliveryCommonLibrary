package com.fooddelivery.common.client;

import com.fooddelivery.common.dto.wallet.WalletDto;
import com.fooddelivery.common.dto.wallet.TransactionRequest;
import com.fooddelivery.common.enums.WalletEntityType;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.List;
import java.util.Map;

@Component
public class WalletInternalClientFallback implements WalletInternalClient {

    @Override
    public WalletDto getWallet(WalletEntityType entityType, UUID entityId) {
        throw new RuntimeException("Wallet Service is currently unavailable");
    }

    @Override
    public WalletDto credit(WalletEntityType entityType, UUID entityId, TransactionRequest request, String callingService) {
        throw new RuntimeException("Wallet Service is currently unavailable");
    }

    @Override
    public WalletDto debit(WalletEntityType entityType, UUID entityId, TransactionRequest request, String callingService) {
        throw new RuntimeException("Wallet Service is currently unavailable");
    }
}
