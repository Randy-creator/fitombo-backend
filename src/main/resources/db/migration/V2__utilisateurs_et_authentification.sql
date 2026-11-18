-- Comptes, profils et authentification

-- Le numéro de téléphone sert d’identifiant de connexion.
CREATE TABLE utilisateur (
    id                    UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    nom                   VARCHAR(150) NOT NULL,
    telephone             VARCHAR(20)  NOT NULL,
    email                 VARCHAR(255),
    mot_de_passe_hash     VARCHAR(255),
    langue                VARCHAR(2)   NOT NULL DEFAULT 'mg',
    role                  VARCHAR(20)  NOT NULL DEFAULT 'ELEVEUR',
    telephone_verifie     BOOLEAN      NOT NULL DEFAULT FALSE,
    actif                 BOOLEAN      NOT NULL DEFAULT TRUE,
    derniere_connexion_at TIMESTAMPTZ,
    version               BIGINT       NOT NULL DEFAULT 0,
    created_at            TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted_at            TIMESTAMPTZ,
    CONSTRAINT ck_utilisateur_telephone CHECK (telephone ~ '^\+[1-9][0-9]{7,14}$'),
    CONSTRAINT ck_utilisateur_langue    CHECK (langue IN ('mg', 'fr')),
    CONSTRAINT ck_utilisateur_role      CHECK (role IN ('ADMIN', 'ELEVEUR', 'EMPLOYE', 'VETERINAIRE', 'ORGANISATION'))
);

CREATE UNIQUE INDEX uq_utilisateur_telephone ON utilisateur (telephone)
    WHERE deleted_at IS NULL;
CREATE UNIQUE INDEX uq_utilisateur_email ON utilisateur (lower(email))
    WHERE email IS NOT NULL AND deleted_at IS NULL;

COMMENT ON TABLE  utilisateur           IS 'Compte de connexion. Le téléphone (format E.164, ex. +261340000000) sert d''identifiant.';
COMMENT ON COLUMN utilisateur.role      IS 'Rôle global. Les rôles par exploitation sont dans membre_exploitation.';
COMMENT ON COLUMN utilisateur.mot_de_passe_hash IS 'Optionnel : connexion possible uniquement par code SMS.';

-- organisation : coopérative, ONG, projet, institution
CREATE TABLE organisation (
    id         UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    nom        VARCHAR(200) NOT NULL,
    type       VARCHAR(20)  NOT NULL,
    region     VARCHAR(100),
    telephone  VARCHAR(20),
    email      VARCHAR(255),
    version    BIGINT       NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted_at TIMESTAMPTZ,
    CONSTRAINT ck_organisation_type CHECK (type IN ('COOPERATIVE', 'ONG', 'PROJET', 'INSTITUTION', 'AUTRE'))
);

CREATE UNIQUE INDEX uq_organisation_nom ON organisation (lower(nom)) WHERE deleted_at IS NULL;

-- Un utilisateur peut avoir un profil d’éleveur.
CREATE TABLE eleveur (
    id                             UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    utilisateur_id                 UUID         NOT NULL REFERENCES utilisateur (id),
    organisation_id                UUID         REFERENCES organisation (id) ON DELETE SET NULL,
    region                         VARCHAR(100),
    district                       VARCHAR(100),
    commune                        VARCHAR(100),
    fokontany                      VARCHAR(100),
    date_inscription               DATE         NOT NULL DEFAULT CURRENT_DATE,
    partage_donnees_organisation   BOOLEAN      NOT NULL DEFAULT FALSE,
    version                        BIGINT       NOT NULL DEFAULT 0,
    created_at                     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at                     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted_at                     TIMESTAMPTZ
);

CREATE UNIQUE INDEX uq_eleveur_utilisateur ON eleveur (utilisateur_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_eleveur_organisation ON eleveur (organisation_id);

COMMENT ON COLUMN eleveur.partage_donnees_organisation IS
    'RG-13 : l''organisation ne voit les statistiques de l''éleveur que si TRUE (consentement révocable).';

-- Appareils qui reçoivent les notifications push
CREATE TABLE appareil (
    id                   UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    utilisateur_id       UUID         NOT NULL REFERENCES utilisateur (id) ON DELETE CASCADE,
    nom                  VARCHAR(150),
    plateforme           VARCHAR(10)  NOT NULL,
    token_push           VARCHAR(512),
    derniere_activite_at TIMESTAMPTZ,
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted_at           TIMESTAMPTZ,
    CONSTRAINT ck_appareil_plateforme CHECK (plateforme IN ('ANDROID', 'IOS', 'WEB'))
);

CREATE INDEX idx_appareil_utilisateur ON appareil (utilisateur_id);
CREATE UNIQUE INDEX uq_appareil_token_push ON appareil (token_push)
    WHERE token_push IS NOT NULL AND deleted_at IS NULL;

-- Les jetons de rafraîchissement sont renouvelables et révocables. Seul leur hash est stocké.
CREATE TABLE jeton_rafraichissement (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    utilisateur_id  UUID         NOT NULL REFERENCES utilisateur (id) ON DELETE CASCADE,
    appareil_id     UUID         REFERENCES appareil (id) ON DELETE SET NULL,
    jeton_hash      VARCHAR(128) NOT NULL,
    expire_at       TIMESTAMPTZ  NOT NULL,
    revoque_at      TIMESTAMPTZ,
    remplace_par_id UUID         REFERENCES jeton_rafraichissement (id) ON DELETE SET NULL,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_jeton_hash UNIQUE (jeton_hash)
);

CREATE INDEX idx_jeton_utilisateur ON jeton_rafraichissement (utilisateur_id, revoque_at);
CREATE INDEX idx_jeton_expiration  ON jeton_rafraichissement (expire_at);

-- code_verification : codes SMS a usage unique (inscription, connexion, récupération)
CREATE TABLE code_verification (
    id         UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    telephone  VARCHAR(20)  NOT NULL,
    code_hash  VARCHAR(128) NOT NULL,
    objectif   VARCHAR(20)  NOT NULL,
    expire_at  TIMESTAMPTZ  NOT NULL,
    tentatives SMALLINT     NOT NULL DEFAULT 0,
    utilise_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT ck_code_verification_objectif   CHECK (objectif IN ('INSCRIPTION', 'CONNEXION', 'RECUPERATION')),
    CONSTRAINT ck_code_verification_tentatives CHECK (tentatives >= 0),
    CONSTRAINT ck_code_verification_telephone  CHECK (telephone ~ '^\+[1-9][0-9]{7,14}$')
);

CREATE INDEX idx_code_verification_telephone ON code_verification (telephone, created_at DESC);

-- Mise a jour automatique de updated_at
CREATE TRIGGER trg_utilisateur_updated_at  BEFORE UPDATE ON utilisateur  FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_organisation_updated_at BEFORE UPDATE ON organisation FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_eleveur_updated_at      BEFORE UPDATE ON eleveur      FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_appareil_updated_at     BEFORE UPDATE ON appareil     FOR EACH ROW EXECUTE FUNCTION set_updated_at();
