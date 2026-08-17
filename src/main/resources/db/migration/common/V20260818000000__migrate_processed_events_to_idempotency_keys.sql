CREATE TABLE IF NOT EXISTS idempotency_keys (
    idempotency_key VARCHAR(255) PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

DO $$
BEGIN
    IF EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'processed_events') THEN
        INSERT INTO idempotency_keys (idempotency_key, created_at)
        SELECT event_id, processed_at FROM processed_events
        ON CONFLICT (idempotency_key) DO NOTHING;
        
        DROP TABLE processed_events;
    END IF;
END $$;
