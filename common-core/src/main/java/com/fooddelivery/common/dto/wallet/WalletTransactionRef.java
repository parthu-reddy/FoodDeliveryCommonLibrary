package com.fooddelivery.common.dto.wallet;

import java.time.Instant;
import java.util.UUID;

/**
 * What a caller reads back about one wallet movement ({@code GET /api/v1/internal/wallets/transactions/reference/{id}}):
 * the reference it was made under and when it happened, which WalletService writes as the instant its caller stated
 * (ClearingReconResiduals C4). The response carries more fields; a caller binds only these.
 */
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
public record WalletTransactionRef(UUID referenceId, Instant createdAt) {
}
