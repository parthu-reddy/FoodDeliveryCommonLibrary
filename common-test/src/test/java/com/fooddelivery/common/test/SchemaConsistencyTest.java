package com.fooddelivery.common.test;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class SchemaConsistencyTest {

    @Test
    void parsesEveryAddColumnClauseInOneAlterTableStatement() {
        Map<String, Map<String, String>> schema = SchemaConsistency.parseSql(List.of("""
                CREATE TABLE orders (
                    id UUID PRIMARY KEY
                );

                ALTER TABLE orders
                    ADD COLUMN IF NOT EXISTS manual_intervention_operation_id VARCHAR(192),
                    ADD COLUMN IF NOT EXISTS manual_intervention_requested_driver_id UUID,
                    ADD COLUMN IF NOT EXISTS manual_intervention_requested_at TIMESTAMPTZ;
                """));

        assertEquals("varchar(192)", schema.get("orders").get("manual_intervention_operation_id"));
        assertEquals("uuid", schema.get("orders").get("manual_intervention_requested_driver_id"));
        assertEquals("timestamptz", schema.get("orders").get("manual_intervention_requested_at"));
    }

    @Test
    void doesNotTreatAnAlterTableConstraintAsAColumn() {
        Map<String, Map<String, String>> schema = SchemaConsistency.parseSql(List.of("""
                CREATE TABLE payout_operations (
                    id UUID PRIMARY KEY
                );

                ALTER TABLE payout_operations
                    ADD CONSTRAINT payout_operations_idempotency_key_unique UNIQUE (id);
                """));

        assertFalse(schema.get("payout_operations").containsKey("constraint"));
    }
}
