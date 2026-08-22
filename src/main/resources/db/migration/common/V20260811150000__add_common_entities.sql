CREATE TABLE idempotency_keys (
    idempotency_key VARCHAR(255) PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE outbox_events (
    id UUID PRIMARY KEY,
    aggregate_type VARCHAR(50) NOT NULL,
    aggregate_id VARCHAR(50) NOT NULL,
    type VARCHAR(50) NOT NULL,
    payload JSONB NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    retry_count INT NOT NULL DEFAULT 0,
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP,
    idempotency_key VARCHAR(255) UNIQUE
);

CREATE TABLE idempotency_keys (
    idempotency_key VARCHAR(255) PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_outbox_events_status ON outbox_events(status);

CREATE INDEX IF NOT EXISTS idx_outbox_events_created_at ON outbox_events(created_at);

DO $$
BEGIN
    IF EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'processed_events') THEN
        INSERT INTO idempotency_keys (idempotency_key, created_at)
        SELECT event_id, processed_at FROM processed_events
        ON CONFLICT (idempotency_key) DO NOTHING;

DROP TABLE processed_events;

END IF;

END $$;

DO $$ BEGIN
  IF EXISTS (SELECT 1 FROM information_schema.columns
              WHERE table_name = 'idempotency_keys' AND column_name = 'key') THEN
    ALTER TABLE idempotency_keys RENAME COLUMN key TO idempotency_key;

END IF;

END $$;
