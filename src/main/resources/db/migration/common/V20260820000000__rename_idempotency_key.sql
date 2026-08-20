DO $$ BEGIN
  IF EXISTS (SELECT 1 FROM information_schema.columns
              WHERE table_name = 'idempotency_keys' AND column_name = 'key') THEN
    ALTER TABLE idempotency_keys RENAME COLUMN key TO idempotency_key;
  END IF;
END $$;
