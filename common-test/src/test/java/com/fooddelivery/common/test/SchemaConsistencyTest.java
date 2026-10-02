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

    @Test
    void requiredColumnsAreNotNullWithoutADefaultAndNotGenerated() {
        Map<String, java.util.Set<String>> required = SchemaConsistency.parseRequiredSql(List.of("""
                CREATE TABLE refund_items (
                    id UUID PRIMARY KEY,
                    refund_id UUID NOT NULL REFERENCES refunds(id),
                    quantity INTEGER NOT NULL,
                    amount DECIMAL(15,2) NOT NULL,
                    note TEXT,
                    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                    seq BIGSERIAL,
                    UNIQUE (refund_id, quantity)
                );
                """));
        assertEquals(java.util.Set.of("id", "refund_id", "quantity", "amount"), required.get("refund_items"));
    }

    @Test
    void laterMigrationsAddRelaxDropAndRenameRequirements() {
        Map<String, java.util.Set<String>> required = SchemaConsistency.parseRequiredSql(List.of("""
                CREATE TABLE ledger_entries (
                    id UUID NOT NULL,
                    transaction_id UUID NOT NULL,
                    amount NUMERIC(14, 2) NOT NULL,
                    legacy TEXT NOT NULL,
                    renamed_from TEXT NOT NULL,
                    PRIMARY KEY (id)
                );
                """, """
                ALTER TABLE ledger_entries ADD COLUMN IF NOT EXISTS leg_index INT;
                ALTER TABLE ledger_entries ALTER COLUMN leg_index SET NOT NULL;
                ALTER TABLE ledger_entries ADD COLUMN source VARCHAR(20) NOT NULL DEFAULT 'x', ADD COLUMN batch INT NOT NULL;
                ALTER TABLE ledger_entries DROP COLUMN IF EXISTS legacy;
                ALTER TABLE ledger_entries ALTER COLUMN amount DROP NOT NULL;
                ALTER TABLE ledger_entries RENAME COLUMN renamed_from TO renamed_to;
                ALTER TABLE ledger_entries ALTER COLUMN batch SET DEFAULT 0;
                ALTER TABLE ledger_entries ADD CONSTRAINT k UNIQUE (transaction_id, leg_index);
                """));
        assertEquals(java.util.Set.of("id", "transaction_id", "leg_index", "renamed_to"), required.get("ledger_entries"));
    }

    @jakarta.persistence.MappedSuperclass
    static class Audited {
        @jakarta.persistence.Id java.util.UUID id;
        @jakarta.persistence.Column(name = "created_at") java.time.Instant createdAt;
    }

    @jakarta.persistence.Embeddable
    static class Money {
        java.math.BigDecimal amount;
        String currency;
    }

    @jakarta.persistence.Entity
    @jakarta.persistence.Table(name = "refunds")
    static class RefundFixture extends Audited {
        @jakarta.persistence.Column(name = "order_id") java.util.UUID orderId;
        @jakarta.persistence.Embedded Money money;
        @jakarta.persistence.Column(name = "computed", insertable = false) String computed;
        @jakarta.persistence.Transient String scratch;
    }

    @Test
    void writtenColumnsIncludeInheritedAndEmbeddedButNotReadOnlyColumns() {
        assertEquals(java.util.Set.of("id", "created_at", "order_id", "amount", "currency"),
                SchemaConsistency.writtenColumns(RefundFixture.class));
    }
}
