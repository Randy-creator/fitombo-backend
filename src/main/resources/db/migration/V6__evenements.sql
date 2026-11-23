-- Journal des événements
-- Une correction annule la saisie d’origine et crée un événement correctif lié.
-- Chaque événement concerne un seul animal ou lot (RG-03).
-- Les tables spécialisées reprennent le type pour vérifier leur lien avec le journal.
-- JPA utilise l’héritage JOINED avec type comme discriminant.

CREATE TABLE evenement (
    id                    UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    exploitation_id       UUID         NOT NULL REFERENCES exploitation (id),
    type                  VARCHAR(25)  NOT NULL,
    animal_id             UUID,
    lot_id                UUID,
    date_evenement        DATE         NOT NULL,
    notes                 TEXT,
    saisi_par             UUID         NOT NULL REFERENCES utilisateur (id),
    statut                VARCHAR(10)  NOT NULL DEFAULT 'VALIDE',
    corrige_evenement_id  UUID         REFERENCES evenement (id),
    motif_annulation      TEXT,
    horodatage_client     TIMESTAMPTZ,
    version               BIGINT       NOT NULL DEFAULT 0,
    created_at            TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted_at            TIMESTAMPTZ,
    CONSTRAINT ck_evenement_type   CHECK (type IN ('PESEE', 'VACCINATION', 'TRAITEMENT', 'ALIMENTATION', 'SAILLIE',
                                                   'DIAGNOSTIC_GESTATION', 'MISE_BAS', 'PRODUCTION', 'MOUVEMENT', 'DECES')),
    CONSTRAINT ck_evenement_statut CHECK (statut IN ('VALIDE', 'ANNULE')),
    -- RG-03 — Une seule cible par événement.
    CONSTRAINT ck_evenement_cible  CHECK (num_nonnulls(animal_id, lot_id) = 1),
    CONSTRAINT ck_evenement_correction CHECK (corrige_evenement_id IS DISTINCT FROM id),
    -- L’historique suit l’animal ou le lot lors d’un changement d’exploitation.
    CONSTRAINT fk_evenement_animal FOREIGN KEY (animal_id, exploitation_id)
        REFERENCES animal (id, exploitation_id) ON UPDATE CASCADE,
    CONSTRAINT fk_evenement_lot    FOREIGN KEY (lot_id, exploitation_id)
        REFERENCES lot (id, exploitation_id) ON UPDATE CASCADE,
    CONSTRAINT uq_evenement_id_type UNIQUE (id, type)
);

CREATE INDEX idx_evenement_animal_date   ON evenement (animal_id, date_evenement DESC) WHERE animal_id IS NOT NULL;
CREATE INDEX idx_evenement_lot_date      ON evenement (lot_id, date_evenement DESC)    WHERE lot_id IS NOT NULL;
CREATE INDEX idx_evenement_expl_type     ON evenement (exploitation_id, type, date_evenement DESC);
CREATE INDEX idx_evenement_updated_at    ON evenement (updated_at);
CREATE INDEX idx_evenement_saisi_par     ON evenement (saisi_par);
CREATE INDEX idx_evenement_correction    ON evenement (corrige_evenement_id) WHERE corrige_evenement_id IS NOT NULL;

COMMENT ON COLUMN evenement.horodatage_client IS 'Heure de saisie sur le téléphone, utile pour les saisies faites hors ligne.';
COMMENT ON COLUMN evenement.statut IS 'ANNULE = événement remplacé ou supprimé par erreur (conservé pour la traçabilité).';

CREATE TRIGGER trg_evenement_updated_at BEFORE UPDATE ON evenement FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- pesee : pour un lot, poids_kg = poids moyen d'un échantillon
CREATE TABLE pesee (
    evenement_id           UUID         PRIMARY KEY,
    type                   VARCHAR(25)  NOT NULL DEFAULT 'PESEE',
    poids_kg               NUMERIC(9,3) NOT NULL,
    methode                VARCHAR(12)  NOT NULL DEFAULT 'BALANCE',
    perimetre_thoracique_cm NUMERIC(6,1),
    longueur_cm            NUMERIC(6,1),
    nb_echantillon         INTEGER,
    CONSTRAINT ck_pesee_type     CHECK (type = 'PESEE'),
    CONSTRAINT ck_pesee_poids    CHECK (poids_kg > 0),
    CONSTRAINT ck_pesee_methode  CHECK (methode IN ('BALANCE', 'RUBAN', 'ESTIMATION')),
    -- RG-09 — La pesée au ruban exige le tour de poitrine et la longueur.
    CONSTRAINT ck_pesee_ruban    CHECK (methode <> 'RUBAN' OR (perimetre_thoracique_cm IS NOT NULL AND longueur_cm IS NOT NULL)),
    CONSTRAINT ck_pesee_mesures  CHECK ((perimetre_thoracique_cm IS NULL OR perimetre_thoracique_cm > 0)
                                    AND (longueur_cm IS NULL OR longueur_cm > 0)),
    CONSTRAINT ck_pesee_echantillon CHECK (nb_echantillon IS NULL OR nb_echantillon > 0),
    CONSTRAINT fk_pesee_evenement FOREIGN KEY (evenement_id, type) REFERENCES evenement (id, type) ON DELETE CASCADE
);

-- vaccination
CREATE TABLE vaccination (
    evenement_id         UUID         PRIMARY KEY,
    type                 VARCHAR(25)  NOT NULL DEFAULT 'VACCINATION',
    vaccin               VARCHAR(200) NOT NULL,
    maladie_ciblee       VARCHAR(150),
    lot_vaccin           VARCHAR(100),
    rappel_prevu         DATE,
    etape_protocole_id   UUID         REFERENCES etape_protocole (id) ON DELETE SET NULL,
    CONSTRAINT ck_vaccination_type CHECK (type = 'VACCINATION'),
    CONSTRAINT fk_vaccination_evenement FOREIGN KEY (evenement_id, type) REFERENCES evenement (id, type) ON DELETE CASCADE
);

CREATE INDEX idx_vaccination_rappel ON vaccination (rappel_prevu) WHERE rappel_prevu IS NOT NULL;

-- Un trigger calcule la fin du délai d’attente à partir de la date et de la durée du traitement.
CREATE TABLE traitement (
    evenement_id         UUID         PRIMARY KEY,
    type                 VARCHAR(25)  NOT NULL DEFAULT 'TRAITEMENT',
    maladie              VARCHAR(150),
    produit              VARCHAR(200) NOT NULL,
    dose                 VARCHAR(100),
    duree_jours          INTEGER,
    delai_attente_jours  INTEGER      NOT NULL DEFAULT 0,
    fin_delai_attente    DATE,
    suspicion_maladie_declarable BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT ck_traitement_type  CHECK (type = 'TRAITEMENT'),
    CONSTRAINT ck_traitement_duree CHECK (duree_jours IS NULL OR duree_jours > 0),
    CONSTRAINT ck_traitement_delai CHECK (delai_attente_jours >= 0),
    CONSTRAINT fk_traitement_evenement FOREIGN KEY (evenement_id, type) REFERENCES evenement (id, type) ON DELETE CASCADE
);

CREATE INDEX idx_traitement_fin_delai ON traitement (fin_delai_attente) WHERE fin_delai_attente IS NOT NULL;

COMMENT ON COLUMN traitement.fin_delai_attente IS
    'RG-10 : avant cette date, la vente, l''abattage ou la consommation du lait déclenche un avertissement.';

CREATE OR REPLACE FUNCTION calculer_fin_delai_attente()
RETURNS TRIGGER AS $$
DECLARE
    date_soin DATE;
BEGIN
    SELECT date_evenement INTO date_soin FROM evenement WHERE id = NEW.evenement_id;
    IF date_soin IS NOT NULL THEN
        NEW.fin_delai_attente := date_soin + COALESCE(NEW.duree_jours, 0) + NEW.delai_attente_jours;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_traitement_fin_delai
    BEFORE INSERT OR UPDATE OF duree_jours, delai_attente_jours ON traitement
    FOR EACH ROW EXECUTE FUNCTION calculer_fin_delai_attente();

-- alimentation
CREATE TABLE alimentation (
    evenement_id  UUID          PRIMARY KEY,
    type          VARCHAR(25)   NOT NULL DEFAULT 'ALIMENTATION',
    aliment       VARCHAR(150)  NOT NULL,
    quantite_kg   NUMERIC(10,3) NOT NULL,
    cout_ariary   NUMERIC(14,2),
    CONSTRAINT ck_alimentation_type     CHECK (type = 'ALIMENTATION'),
    CONSTRAINT ck_alimentation_quantite CHECK (quantite_kg > 0),
    CONSTRAINT ck_alimentation_cout     CHECK (cout_ariary IS NULL OR cout_ariary >= 0),
    CONSTRAINT fk_alimentation_evenement FOREIGN KEY (evenement_id, type) REFERENCES evenement (id, type) ON DELETE CASCADE
);

-- La saillie concerne la femelle. Le mâle peut venir de l’extérieur.
CREATE TABLE saillie (
    evenement_id           UUID         PRIMARY KEY,
    type                   VARCHAR(25)  NOT NULL DEFAULT 'SAILLIE',
    methode                VARCHAR(25)  NOT NULL DEFAULT 'MONTE_NATURELLE',
    male_id                UUID         REFERENCES animal (id) ON DELETE SET NULL,
    male_externe           VARCHAR(200),
    reference_semence      VARCHAR(100),
    mise_bas_prevue        DATE,
    CONSTRAINT ck_saillie_type    CHECK (type = 'SAILLIE'),
    CONSTRAINT ck_saillie_methode CHECK (methode IN ('MONTE_NATURELLE', 'INSEMINATION_ARTIFICIELLE')),
    CONSTRAINT fk_saillie_evenement FOREIGN KEY (evenement_id, type) REFERENCES evenement (id, type) ON DELETE CASCADE
);

CREATE INDEX idx_saillie_male            ON saillie (male_id) WHERE male_id IS NOT NULL;
CREATE INDEX idx_saillie_mise_bas_prevue ON saillie (mise_bas_prevue) WHERE mise_bas_prevue IS NOT NULL;

COMMENT ON COLUMN saillie.mise_bas_prevue IS 'RG-11 : date de saillie + durée de gestation de l''espèce.';

-- diagnostic_gestation
CREATE TABLE diagnostic_gestation (
    evenement_id  UUID         PRIMARY KEY,
    type          VARCHAR(25)  NOT NULL DEFAULT 'DIAGNOSTIC_GESTATION',
    positif       BOOLEAN      NOT NULL,
    methode       VARCHAR(15)  NOT NULL DEFAULT 'OBSERVATION',
    saillie_id    UUID         REFERENCES saillie (evenement_id) ON DELETE SET NULL,
    CONSTRAINT ck_diag_type    CHECK (type = 'DIAGNOSTIC_GESTATION'),
    CONSTRAINT ck_diag_methode CHECK (methode IN ('PALPATION', 'ECHOGRAPHIE', 'OBSERVATION', 'TEST', 'AUTRE')),
    CONSTRAINT fk_diag_evenement FOREIGN KEY (evenement_id, type) REFERENCES evenement (id, type) ON DELETE CASCADE
);

CREATE INDEX idx_diag_saillie ON diagnostic_gestation (saillie_id) WHERE saillie_id IS NOT NULL;

-- mise_bas : les petits sont des animaux (animal.mise_bas_id) ou un lot (lot_petits_id)
CREATE TABLE mise_bas (
    evenement_id   UUID         PRIMARY KEY,
    type           VARCHAR(25)  NOT NULL DEFAULT 'MISE_BAS',
    nes_vivants    INTEGER      NOT NULL DEFAULT 0,
    morts_nes      INTEGER      NOT NULL DEFAULT 0,
    assistance     BOOLEAN      NOT NULL DEFAULT FALSE,
    saillie_id     UUID         REFERENCES saillie (evenement_id) ON DELETE SET NULL,
    lot_petits_id  UUID         REFERENCES lot (id) ON DELETE SET NULL,
    CONSTRAINT ck_mise_bas_type    CHECK (type = 'MISE_BAS'),
    CONSTRAINT ck_mise_bas_nombres CHECK (nes_vivants >= 0 AND morts_nes >= 0 AND nes_vivants + morts_nes > 0),
    CONSTRAINT fk_mise_bas_evenement FOREIGN KEY (evenement_id, type) REFERENCES evenement (id, type) ON DELETE CASCADE
);

CREATE INDEX idx_mise_bas_saillie ON mise_bas (saillie_id) WHERE saillie_id IS NOT NULL;

-- Chaque petit suivi individuellement est lié à sa mise bas.
ALTER TABLE animal ADD COLUMN mise_bas_id UUID REFERENCES mise_bas (evenement_id) ON DELETE SET NULL;
CREATE INDEX idx_animal_mise_bas ON animal (mise_bas_id) WHERE mise_bas_id IS NOT NULL;

-- production : lait, oeufs, fumier, miel...
CREATE TABLE production (
    evenement_id      UUID          PRIMARY KEY,
    type              VARCHAR(25)   NOT NULL DEFAULT 'PRODUCTION',
    type_production   VARCHAR(10)   NOT NULL,
    quantite          NUMERIC(12,3) NOT NULL,
    unite             VARCHAR(10)   NOT NULL,
    CONSTRAINT ck_production_type     CHECK (type = 'PRODUCTION'),
    CONSTRAINT ck_production_genre    CHECK (type_production IN ('LAIT', 'OEUFS', 'FUMIER', 'MIEL', 'LAINE', 'AUTRE')),
    CONSTRAINT ck_production_quantite CHECK (quantite >= 0),
    CONSTRAINT ck_production_unite    CHECK (unite IN ('LITRE', 'KG', 'UNITE')),
    CONSTRAINT fk_production_evenement FOREIGN KEY (evenement_id, type) REFERENCES evenement (id, type) ON DELETE CASCADE
);

-- mouvement : achat, vente, transfert, abattage, confiage
CREATE TABLE mouvement (
    evenement_id                 UUID          PRIMARY KEY,
    type                         VARCHAR(25)   NOT NULL DEFAULT 'MOUVEMENT',
    type_mouvement               VARCHAR(20)   NOT NULL,
    quantite                     INTEGER       NOT NULL DEFAULT 1,
    prix_total_ariary            NUMERIC(14,2),
    mode_prix                    VARCHAR(20),
    poids_total_kg               NUMERIC(10,2),
    numero_piece_circulation     VARCHAR(100),
    tiers_id                     UUID          REFERENCES tiers (id) ON DELETE SET NULL,
    emplacement_destination_id   UUID          REFERENCES emplacement (id) ON DELETE SET NULL,
    exploitation_destination_id  UUID          REFERENCES exploitation (id),
    CONSTRAINT ck_mouvement_type      CHECK (type = 'MOUVEMENT'),
    CONSTRAINT ck_mouvement_genre     CHECK (type_mouvement IN ('ACHAT', 'VENTE', 'TRANSFERT', 'ABATTAGE', 'CONFIAGE', 'RETOUR_CONFIAGE')),
    CONSTRAINT ck_mouvement_quantite  CHECK (quantite >= 1),
    CONSTRAINT ck_mouvement_prix      CHECK (prix_total_ariary IS NULL OR prix_total_ariary >= 0),
    CONSTRAINT ck_mouvement_mode_prix CHECK (mode_prix IS NULL OR mode_prix IN ('POIDS_VIF', 'TETE', 'POIDS_CARCASSE', 'FORFAIT')),
    CONSTRAINT ck_mouvement_poids     CHECK (poids_total_kg IS NULL OR poids_total_kg > 0),
    CONSTRAINT fk_mouvement_evenement FOREIGN KEY (evenement_id, type) REFERENCES evenement (id, type) ON DELETE CASCADE
);

CREATE INDEX idx_mouvement_tiers ON mouvement (tiers_id) WHERE tiers_id IS NOT NULL;
CREATE INDEX idx_mouvement_genre ON mouvement (type_mouvement);

COMMENT ON COLUMN mouvement.numero_piece_circulation IS
    'Numéro de la pièce de circulation ou de propriété exigée lors des ventes et déplacements de bétail (à vérifier auprès du MAEP/DSV).';

-- deces : mort, vol, perte
CREATE TABLE deces (
    evenement_id        UUID         PRIMARY KEY,
    type                VARCHAR(25)  NOT NULL DEFAULT 'DECES',
    cause               VARCHAR(15)  NOT NULL DEFAULT 'INCONNUE',
    nombre              INTEGER      NOT NULL DEFAULT 1,
    maladie_suspectee   VARCHAR(150),
    declaration_faite   BOOLEAN      NOT NULL DEFAULT FALSE,
    CONSTRAINT ck_deces_type   CHECK (type = 'DECES'),
    CONSTRAINT ck_deces_cause  CHECK (cause IN ('MALADIE', 'ACCIDENT', 'PREDATION', 'VOL', 'PERTE', 'INCONNUE', 'AUTRE')),
    CONSTRAINT ck_deces_nombre CHECK (nombre >= 1),
    CONSTRAINT fk_deces_evenement FOREIGN KEY (evenement_id, type) REFERENCES evenement (id, type) ON DELETE CASCADE
);

COMMENT ON COLUMN deces.declaration_faite IS 'TRUE si une suspicion de maladie à déclaration obligatoire a été signalée aux services vétérinaires.';
