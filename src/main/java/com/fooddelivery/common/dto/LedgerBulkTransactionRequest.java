package com.fooddelivery.common.dto;

import java.util.List;
import java.util.UUID;

public class LedgerBulkTransactionRequest {
    private UUID referenceId;
    private List<LedgerEntryCommand> entries;

    public LedgerBulkTransactionRequest() {}

    public LedgerBulkTransactionRequest(UUID referenceId, List<LedgerEntryCommand> entries) {
        this.referenceId = referenceId;
        this.entries = entries;
    }

    public UUID getReferenceId() { return referenceId; }
    public void setReferenceId(UUID referenceId) { this.referenceId = referenceId; }

    public List<LedgerEntryCommand> getEntries() { return entries; }
    public void setEntries(List<LedgerEntryCommand> entries) { this.entries = entries; }
}
