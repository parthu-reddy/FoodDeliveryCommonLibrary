ALTER TABLE outbox_events ADD COLUMN idempotency_key VARCHAR(255) UNIQUE;
