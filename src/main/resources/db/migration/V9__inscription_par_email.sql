ALTER TABLE utilisateur
    ALTER COLUMN telephone DROP NOT NULL,
    ADD COLUMN email_verifie BOOLEAN NOT NULL DEFAULT FALSE,
    ADD CONSTRAINT ck_utilisateur_contact CHECK (telephone IS NOT NULL OR email IS NOT NULL),
    ADD CONSTRAINT ck_utilisateur_email_verifie CHECK (NOT email_verifie OR email IS NOT NULL);

CREATE TABLE code_verification_email (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email       VARCHAR(255) NOT NULL,
    code_hash   VARCHAR(128) NOT NULL,
    expire_at   TIMESTAMPTZ NOT NULL,
    tentatives  SMALLINT NOT NULL DEFAULT 0 CHECK (tentatives >= 0),
    utilise_at  TIMESTAMPTZ,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_code_verification_email_recents ON code_verification_email (email, created_at DESC);

COMMENT ON COLUMN utilisateur.mot_de_passe_hash IS 'Hash BCrypt utilisé pour la connexion d’un compte vérifié.';
