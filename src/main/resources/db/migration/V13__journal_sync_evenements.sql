CREATE TABLE journal_sync_evenement (
    sequence BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    transaction_id XID8 NOT NULL DEFAULT pg_current_xact_id(),
    exploitation_id UUID NOT NULL REFERENCES exploitation(id) ON DELETE CASCADE,
    evenement_id UUID NOT NULL
);

CREATE INDEX idx_journal_sync_exploitation_transaction
    ON journal_sync_evenement (exploitation_id, transaction_id, sequence);

CREATE FUNCTION journaliser_evenement_sync()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    IF TG_OP = 'DELETE' THEN
        INSERT INTO journal_sync_evenement(exploitation_id, evenement_id)
        VALUES (OLD.exploitation_id, OLD.id);
        RETURN OLD;
    END IF;

    IF TG_OP = 'UPDATE' AND OLD.exploitation_id <> NEW.exploitation_id THEN
        INSERT INTO journal_sync_evenement(exploitation_id, evenement_id)
        VALUES (OLD.exploitation_id, OLD.id);
    END IF;

    INSERT INTO journal_sync_evenement(exploitation_id, evenement_id)
    VALUES (NEW.exploitation_id, NEW.id);
    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_evenement_journal_sync
    AFTER INSERT OR UPDATE OR DELETE ON evenement
    FOR EACH ROW EXECUTE FUNCTION journaliser_evenement_sync();

INSERT INTO journal_sync_evenement(exploitation_id, evenement_id)
SELECT exploitation_id, id FROM evenement WHERE deleted_at IS NULL;
