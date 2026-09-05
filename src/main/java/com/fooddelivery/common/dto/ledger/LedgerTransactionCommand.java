package com.fooddelivery.common.dto.ledger;

import java.util.List;
import java.util.UUID;

public class LedgerTransactionCommand {
    private UUID transactionId;
    private UUID referenceId;
    private String producer;
    private List<LedgerLeg> legs;

    public LedgerTransactionCommand() {}

    public LedgerTransactionCommand(UUID transactionId, UUID referenceId, String producer, List<LedgerLeg> legs) {
        this.transactionId = transactionId;
        this.referenceId = referenceId;
        this.producer = producer;
        this.legs = legs;
    }

    public UUID getTransactionId() { return transactionId; }
    public void setTransactionId(UUID transactionId) { this.transactionId = transactionId; }

    public UUID getReferenceId() { return referenceId; }
    public void setReferenceId(UUID referenceId) { this.referenceId = referenceId; }

    public String getProducer() { return producer; }
    public void setProducer(String producer) { this.producer = producer; }

    public List<LedgerLeg> getLegs() { return legs; }
    public void setLegs(List<LedgerLeg> legs) { this.legs = legs; }
}
