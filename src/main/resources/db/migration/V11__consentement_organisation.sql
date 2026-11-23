ALTER TABLE utilisateur ADD COLUMN organisation_id UUID REFERENCES organisation(id);
ALTER TABLE eleveur ADD COLUMN organisation_consentie_id UUID REFERENCES organisation(id);

CREATE INDEX idx_eleveur_organisation_consentie
    ON eleveur (organisation_consentie_id) WHERE partage_donnees_organisation;

CREATE FUNCTION revoquer_consentement_changement_organisation()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    NEW.partage_donnees_organisation := FALSE;
    NEW.organisation_consentie_id := NULL;
    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_eleveur_revoquer_consentement
    BEFORE UPDATE OF organisation_id ON eleveur
    FOR EACH ROW WHEN (OLD.organisation_id IS DISTINCT FROM NEW.organisation_id)
    EXECUTE FUNCTION revoquer_consentement_changement_organisation();
