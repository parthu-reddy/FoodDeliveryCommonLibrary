package com.fooddelivery.common.dto.wallet;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * What an internal debit or credit answers: the movement it made, or -- for a replayed request -- the movement the first
 * attempt made (ReconResidualsC7 Q1). {@link #occurredAt} is that movement's instant, the one its wallet transaction and its
 * ledger posting carry; a caller that dates anything on the movement takes it from here, never from what it asked for.
 */
@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
public class WalletMovementDto {
    private UUID walletId;
    private UUID entityId;
    private com.fooddelivery.common.enums.WalletEntityType entityType;
    private BigDecimal balance;
    private String currency;
    private UUID transactionId;
    /** When the money moved: the first attempt's instant, also on a replay. */
    private java.time.Instant occurredAt;
    /** True when this answers a request whose reference had already moved money; nothing moved this time. */
    private boolean replayed;
}
