-- Fonctions utilitaires
-- Les noms SQL sont en français, en snake_case et sans accent.
-- Le client ou PostgreSQL génère les clés UUID.
-- Les enums JPA sont stockées en texte et contrôlées par des contraintes CHECK.
-- deleted_at marque une suppression logique. Hibernate gère le verrou optimiste via version.
-- Les montants sont en ariary (NUMERIC(14,2)).
-- Le serveur fixe updated_at à chaque modification pour la synchronisation (GET /sync/pull?depuis=...).
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at := now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION set_updated_at() IS
    'Trigger générique : positionne updated_at = now() avant chaque UPDATE.';
