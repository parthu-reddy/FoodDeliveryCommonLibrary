package com.fooddelivery.common.dto.ledger;

import java.util.List;
import java.util.UUID;

/**
 * One ledger movement, addressed by an id the receiver can re-derive.
 *
 * <p>{@link #transactionId} must equal
 * {@code DeterministicIdUtils.ledgerId(producer, referenceId, leg)}. Carrying {@link #leg} is what
 * makes that checkable: the ledger previously accepted any v5 UUID, because it tried two hardcoded
 * leg names and then fell back to a bare version check, so the derivability guarantee the design
 * rested on did not actually hold.
 */
@lombok.AllArgsConstructor
@lombok.NoArgsConstructor
@lombok.Data
public class LedgerTransactionCommand {
    private UUID transactionId;
    private UUID referenceId;
    private String producer;
    /** The movement this id was derived for, e.g. DELIVERED, REFUND, CREATE, PAID, DEBIT. */
    private String leg;
    private List<LedgerLeg> legs;
    /**
     * When the money moved, as the producer dates it: the instant the other side of every reconciliation dates the same
     * movement on (a capture's paymentCapturedAt, a delivery's deliveredAt). Every entry of the transaction carries it and
     * the reconciliation windows read it; the ledger refuses a command without one (ClearingReconResiduals C3, C6).
     */
    private java.time.Instant occurredAt;
}
