package com.fooddelivery.common.dto.ledger;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CashRemittanceDto {
    private UUID id;
    private UUID driverId;
    private BigDecimal amount;
    private String reference;
    private UUID recordedBy;
    private UUID ledgerTransactionId;
    private OffsetDateTime createdAt;
}
