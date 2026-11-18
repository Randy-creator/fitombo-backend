-- Référentiels d’élevage
-- Pour ajouter une espèce ou une race, il suffit d’ajouter une ligne en base.

-- Chaque espèce a un code stable (ex. ZEBU).
CREATE TABLE espece (
    code                        VARCHAR(30)  PRIMARY KEY,
    nom_fr                      VARCHAR(100) NOT NULL,
    nom_mg                      VARCHAR(100),
    mode_suivi                  VARCHAR(10)  NOT NULL,
    duree_gestation_jours       INTEGER,
    duree_incubation_jours      INTEGER,
    frequence_pesee_jours       INTEGER,
    rendement_carcasse_pct      NUMERIC(5,2),
    actif                       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at                  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at                  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT ck_espece_code       CHECK (code ~ '^[A-Z][A-Z0-9_]*$'),
    CONSTRAINT ck_espece_mode_suivi CHECK (mode_suivi IN ('INDIVIDUEL', 'LOT', 'MIXTE')),
    CONSTRAINT ck_espece_gestation  CHECK (duree_gestation_jours  IS NULL OR duree_gestation_jours  > 0),
    CONSTRAINT ck_espece_incubation CHECK (duree_incubation_jours IS NULL OR duree_incubation_jours > 0),
    CONSTRAINT ck_espece_pesee      CHECK (frequence_pesee_jours  IS NULL OR frequence_pesee_jours  > 0),
    CONSTRAINT ck_espece_rendement  CHECK (rendement_carcasse_pct IS NULL OR rendement_carcasse_pct BETWEEN 1 AND 100)
);

COMMENT ON COLUMN espece.mode_suivi IS
    'INDIVIDUEL = un animal = une fiche ; LOT = suivi par bande ; MIXTE = les deux possibles (ex. porcs).';
COMMENT ON COLUMN espece.frequence_pesee_jours IS
    'Fréquence de pesée recommandée, utilisée pour l''alerte PESEE_EN_RETARD.';
COMMENT ON COLUMN espece.rendement_carcasse_pct IS 'Rendement carcasse indicatif (% du poids vif).';

-- race
CREATE TABLE race (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    espece_code VARCHAR(30)  NOT NULL REFERENCES espece (code),
    nom         VARCHAR(120) NOT NULL,
    origine     VARCHAR(100),
    description TEXT,
    actif       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_race_espece_nom   UNIQUE (espece_code, nom),
    -- Permet à animal et lot de vérifier que la race correspond à l'espèce
    CONSTRAINT uq_race_id_espece    UNIQUE (id, espece_code)
);

-- Poids attendu selon l’âge, la race et le sexe
CREATE TABLE courbe_reference (
    id         UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    race_id    UUID         NOT NULL REFERENCES race (id) ON DELETE CASCADE,
    sexe       VARCHAR(10)  NOT NULL DEFAULT 'MIXTE',
    libelle    VARCHAR(150) NOT NULL,
    source     TEXT,
    version    INTEGER      NOT NULL DEFAULT 1,
    valide     BOOLEAN      NOT NULL DEFAULT FALSE,
    actif      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT ck_courbe_sexe    CHECK (sexe IN ('MALE', 'FEMELLE', 'MIXTE')),
    CONSTRAINT ck_courbe_version CHECK (version >= 1),
    CONSTRAINT uq_courbe_race_sexe_libelle UNIQUE (race_id, sexe, libelle)
);

COMMENT ON COLUMN courbe_reference.valide IS
    'TRUE uniquement après validation par un vétérinaire ou un technicien. Les valeurs de départ sont indicatives.';
COMMENT ON COLUMN courbe_reference.source IS 'Source affichée à l''utilisateur.';

CREATE INDEX idx_courbe_race ON courbe_reference (race_id, sexe) WHERE actif;

-- point_courbe : un point (âge, poids attendu, bornes) de la courbe
CREATE TABLE point_courbe (
    id               UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    courbe_id        UUID         NOT NULL REFERENCES courbe_reference (id) ON DELETE CASCADE,
    age_jours        INTEGER      NOT NULL,
    poids_attendu_kg NUMERIC(9,3) NOT NULL,
    poids_min_kg     NUMERIC(9,3) NOT NULL,
    poids_max_kg     NUMERIC(9,3) NOT NULL,
    CONSTRAINT ck_point_age    CHECK (age_jours >= 0),
    CONSTRAINT ck_point_poids  CHECK (poids_min_kg > 0 AND poids_min_kg <= poids_attendu_kg AND poids_attendu_kg <= poids_max_kg),
    CONSTRAINT uq_point_courbe_age UNIQUE (courbe_id, age_jours)
);

-- protocole_sanitaire : calendrier de soins préventifs d'une espèce
CREATE TABLE protocole_sanitaire (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    espece_code VARCHAR(30)  NOT NULL REFERENCES espece (code),
    nom         VARCHAR(150) NOT NULL,
    description TEXT,
    source      TEXT,
    valide      BOOLEAN      NOT NULL DEFAULT FALSE,
    actif       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_protocole_espece_nom UNIQUE (espece_code, nom)
);

COMMENT ON COLUMN protocole_sanitaire.valide IS
    'TRUE uniquement après validation par un vétérinaire. Ne remplace jamais l''avis d''un vétérinaire.';

-- etape_protocole : un acte planifié à un âge donné
CREATE TABLE etape_protocole (
    id                 UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    protocole_id       UUID         NOT NULL REFERENCES protocole_sanitaire (id) ON DELETE CASCADE,
    ordre              SMALLINT     NOT NULL,
    age_jours          INTEGER      NOT NULL,
    type_acte          VARCHAR(25)  NOT NULL,
    produit            VARCHAR(200) NOT NULL,
    maladie_ciblee     VARCHAR(150),
    description        TEXT,
    rappel_apres_jours INTEGER,
    CONSTRAINT ck_etape_age     CHECK (age_jours >= 0),
    CONSTRAINT ck_etape_type    CHECK (type_acte IN ('VACCINATION', 'DEPARASITAGE', 'TRAITEMENT_PREVENTIF', 'AUTRE')),
    CONSTRAINT ck_etape_rappel  CHECK (rappel_apres_jours IS NULL OR rappel_apres_jours > 0),
    CONSTRAINT uq_etape_ordre   UNIQUE (protocole_id, ordre)
);

-- Mise à jour automatique de updated_at
CREATE TRIGGER trg_espece_updated_at              BEFORE UPDATE ON espece              FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_race_updated_at                BEFORE UPDATE ON race                FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_courbe_reference_updated_at    BEFORE UPDATE ON courbe_reference    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_protocole_sanitaire_updated_at BEFORE UPDATE ON protocole_sanitaire FOR EACH ROW EXECUTE FUNCTION set_updated_at();
