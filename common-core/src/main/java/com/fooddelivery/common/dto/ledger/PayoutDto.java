package com.fooddelivery.common.dto.ledger;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayoutDto {
    private UUID id;
    private String payeeType;
    private UUID payeeId;
    private String payeeDisplayName;
    private Instant periodFrom;
    private Instant periodTo;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String beneficiarySnapshot;
    private String bankReference;
    private String failureReason;
    private UUID createdBy;
    private UUID approvedBy;
    private UUID paidBy;
    private String idempotencyKey;
    private UUID ledgerTransactionId;
    private UUID settledTransactionId;
    private Instant createdAt;
    private Instant approvedAt;
    private Instant paidAt;
    private Instant updatedAt;
}
