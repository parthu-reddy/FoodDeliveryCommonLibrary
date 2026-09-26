-- Outbox and idempotency timestamps become TIMESTAMPTZ.
--
-- Why: these columns were naive TIMESTAMPs holding the JVM's wall clock. That is a correct
-- instant only while every JVM runs in UTC, which no configuration guaranteed. The entities now map
-- java.time.Instant, which Hibernate stores as TIMESTAMPTZ.
-- RandomDocuments/TimezoneCorrectness_2026-09-25, Phase 2.
--
-- Direction of USING: every service database gets these columns from
-- V20260811150000__add_common_entities.sql alone, where all three are TIMESTAMP, so
-- "AT TIME ZONE 'UTC'" reads the stored wall clock as UTC. That is what the dev containers wrote,
-- because they run in UTC. (Applied to a column that is already TIMESTAMPTZ it would convert the
-- wrong way. validate_time.py's S-USING check proves that is not the case here.)
--
-- Forward-only: V20260811150000 has been applied and is not edited.

ALTER TABLE outbox_events
    ALTER COLUMN created_at TYPE TIMESTAMPTZ USING created_at AT TIME ZONE 'UTC',
    ALTER COLUMN processed_at TYPE TIMESTAMPTZ USING processed_at AT TIME ZONE 'UTC';

ALTER TABLE idempotency_keys
    ALTER COLUMN created_at TYPE TIMESTAMPTZ USING created_at AT TIME ZONE 'UTC';
