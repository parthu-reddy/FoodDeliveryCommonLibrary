package com.fooddelivery.common.util;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DeterministicIdUtilsLedgerIdTest {

    @Test
    void testLedgerIdIsDeterministicAndV5() {
        String producer = "test-producer";
        String reference = "ref-123";
        String leg = "leg-A";

        UUID id1 = DeterministicIdUtils.ledgerId(producer, reference, leg);
        UUID id2 = DeterministicIdUtils.ledgerId(producer, reference, leg);

        assertEquals(id1, id2, "Ids should be deterministic");
        assertEquals(5, id1.version(), "Should be a v5 UUID");
        
        UUID refUuid = UUID.randomUUID();
        UUID id3 = DeterministicIdUtils.ledgerId(producer, refUuid, leg);
        UUID id4 = DeterministicIdUtils.ledgerId(producer, refUuid.toString(), leg);
        
        assertEquals(id3, id4, "String and UUID reference methods should yield the same result");
    }

    @Test
    void testIsLedgerId() {
        String producer = "test-producer";
        String reference = "ref-123";
        String leg = "leg-A";

        UUID id = DeterministicIdUtils.ledgerId(producer, reference, leg);

        assertTrue(DeterministicIdUtils.isLedgerId(id, producer, reference, leg));
        
        // Different leg
        assertFalse(DeterministicIdUtils.isLedgerId(id, producer, reference, "leg-B"));
        // Random UUID
        assertFalse(DeterministicIdUtils.isLedgerId(UUID.randomUUID(), producer, reference, leg));
        // Different version UUID but same string representation (impossible to generate naturally without overriding)
        // Just checking basic nulls
        assertFalse(DeterministicIdUtils.isLedgerId(null, producer, reference, leg));
    }
}
