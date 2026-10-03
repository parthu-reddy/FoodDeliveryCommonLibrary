CREATE TABLE audit_events (
    id UUID PRIMARY KEY,
    occurred_at TIMESTAMPTZ NOT NULL,
    actor_user_id UUID,
    actor_kind VARCHAR(20) NOT NULL CHECK (actor_kind IN ('USER', 'ADMIN', 'SERVICE')),
    action VARCHAR(60) NOT NULL,
    subject_type VARCHAR(40) NOT NULL,
    subject_id UUID NOT NULL,
    organisation_id UUID,
    reason VARCHAR(500),
    trace_id VARCHAR(64),
    details JSONB
);
CREATE INDEX idx_audit_events_subject ON audit_events (subject_type, subject_id, occurred_at DESC, id);
CREATE INDEX idx_audit_events_org ON audit_events (organisation_id, occurred_at DESC, id);
CREATE FUNCTION audit_events_append_only() RETURNS trigger AS $$
BEGIN RAISE EXCEPTION 'audit_events is append-only'; END $$ LANGUAGE plpgsql;
CREATE TRIGGER audit_events_no_update BEFORE UPDATE OR DELETE ON audit_events
    FOR EACH ROW EXECUTE FUNCTION audit_events_append_only();
