-- Cheptel
-- Un animal suivi individuellement peut aussi appartenir à un lot.

-- Animaux suivis en groupe
CREATE TABLE lot (
    id                      UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    exploitation_id         UUID         NOT NULL REFERENCES exploitation (id),
    espece_code             VARCHAR(30)  NOT NULL REFERENCES espece (code),
    race_id                 UUID,
    emplacement_id          UUID,
    code                    VARCHAR(50)  NOT NULL,
    date_creation           DATE         NOT NULL DEFAULT CURRENT_DATE,
    date_naissance_moyenne  DATE,
    effectif_initial        INTEGER      NOT NULL,
    effectif_actuel         INTEGER      NOT NULL,
    sexe                    VARCHAR(7)   NOT NULL DEFAULT 'MIXTE',
    statut                  VARCHAR(10)  NOT NULL DEFAULT 'ACTIF',
    date_cloture            DATE,
    notes                   TEXT,
    version                 BIGINT       NOT NULL DEFAULT 0,
    created_at              TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted_at              TIMESTAMPTZ,
    CONSTRAINT ck_lot_effectif_initial CHECK (effectif_initial > 0),
    CONSTRAINT ck_lot_effectif_actuel  CHECK (effectif_actuel >= 0),             -- RG-05
    CONSTRAINT ck_lot_sexe             CHECK (sexe IN ('MALE', 'FEMELLE', 'MIXTE')),
    CONSTRAINT ck_lot_statut           CHECK (statut IN ('ACTIF', 'VENDU', 'ABATTU', 'TERMINE', 'PERDU')),
    CONSTRAINT ck_lot_cloture          CHECK ((statut = 'ACTIF') = (date_cloture IS NULL)),
    CONSTRAINT ck_lot_dates            CHECK (date_cloture IS NULL OR date_cloture >= date_creation),
    -- La race doit appartenir à l'espèce du lot
    CONSTRAINT fk_lot_race        FOREIGN KEY (race_id, espece_code) REFERENCES race (id, espece_code),
    -- L'emplacement doit appartenir à la même exploitation
    CONSTRAINT fk_lot_emplacement FOREIGN KEY (emplacement_id, exploitation_id) REFERENCES emplacement (id, exploitation_id),
    CONSTRAINT uq_lot_id_exploitation UNIQUE (id, exploitation_id)
);

CREATE UNIQUE INDEX uq_lot_code ON lot (exploitation_id, lower(code)) WHERE deleted_at IS NULL;
CREATE INDEX idx_lot_exploitation_statut ON lot (exploitation_id, statut);
CREATE INDEX idx_lot_espece              ON lot (espece_code);
CREATE INDEX idx_lot_emplacement         ON lot (emplacement_id);
CREATE INDEX idx_lot_updated_at          ON lot (updated_at);

COMMENT ON COLUMN lot.effectif_actuel IS
    'Diminue à chaque décès, vente ou abattage partiel (RG-05). Maintenu par la couche service.';
COMMENT ON COLUMN lot.date_naissance_moyenne IS
    'Date d''éclosion ou de naissance moyenne : sert à calculer l''âge pour la courbe de référence.';

-- animal : suivi individuel
CREATE TABLE animal (
    id                      UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    exploitation_id         UUID         NOT NULL REFERENCES exploitation (id),
    espece_code             VARCHAR(30)  NOT NULL REFERENCES espece (code),
    race_id                 UUID,
    emplacement_id          UUID,
    lot_id                  UUID,
    mere_id                 UUID         REFERENCES animal (id) ON DELETE SET NULL,
    pere_id                 UUID         REFERENCES animal (id) ON DELETE SET NULL,
    identifiant             VARCHAR(50)  NOT NULL,
    boucle                  VARCHAR(50),
    marque_traditionnelle   VARCHAR(100),
    nom                     VARCHAR(100),
    sexe                    VARCHAR(7)   NOT NULL,
    date_naissance          DATE,
    date_naissance_estimee  BOOLEAN      NOT NULL DEFAULT FALSE,
    origine                 VARCHAR(15)  NOT NULL DEFAULT 'NE_SUR_PLACE',
    statut                  VARCHAR(10)  NOT NULL DEFAULT 'ACTIF',
    date_sortie             DATE,
    signes_distinctifs      TEXT,
    photo_url               VARCHAR(500),
    notes                   TEXT,
    version                 BIGINT       NOT NULL DEFAULT 0,
    created_at              TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted_at              TIMESTAMPTZ,
    CONSTRAINT ck_animal_sexe    CHECK (sexe IN ('MALE', 'FEMELLE')),
    CONSTRAINT ck_animal_origine CHECK (origine IN ('NE_SUR_PLACE', 'ACHETE', 'CONFIE', 'DON', 'HERITAGE')),
    CONSTRAINT ck_animal_statut  CHECK (statut IN ('ACTIF', 'VENDU', 'ABATTU', 'MORT', 'PERDU', 'VOLE', 'REFORME')),
    -- RG-04 — La date de sortie est réservée aux animaux sortis et obligatoire pour eux.
    CONSTRAINT ck_animal_sortie  CHECK ((statut = 'ACTIF') = (date_sortie IS NULL)),
    CONSTRAINT ck_animal_dates   CHECK (date_sortie IS NULL OR date_naissance IS NULL OR date_sortie >= date_naissance),
    -- L’animal, son père et sa mère doivent être distincts.
    CONSTRAINT ck_animal_parents CHECK (mere_id IS DISTINCT FROM id AND pere_id IS DISTINCT FROM id
                                        AND (mere_id IS NULL OR pere_id IS NULL OR mere_id <> pere_id)),
    CONSTRAINT fk_animal_race        FOREIGN KEY (race_id, espece_code)           REFERENCES race (id, espece_code),
    CONSTRAINT fk_animal_emplacement FOREIGN KEY (emplacement_id, exploitation_id) REFERENCES emplacement (id, exploitation_id),
    CONSTRAINT fk_animal_lot         FOREIGN KEY (lot_id, exploitation_id)         REFERENCES lot (id, exploitation_id),
    CONSTRAINT uq_animal_id_exploitation UNIQUE (id, exploitation_id)
);

CREATE UNIQUE INDEX uq_animal_identifiant ON animal (exploitation_id, lower(identifiant)) WHERE deleted_at IS NULL;
CREATE UNIQUE INDEX uq_animal_boucle      ON animal (exploitation_id, lower(boucle))
    WHERE boucle IS NOT NULL AND deleted_at IS NULL;
CREATE INDEX idx_animal_exploitation_statut ON animal (exploitation_id, statut);
CREATE INDEX idx_animal_espece              ON animal (espece_code);
CREATE INDEX idx_animal_lot                 ON animal (lot_id);
CREATE INDEX idx_animal_emplacement         ON animal (emplacement_id);
CREATE INDEX idx_animal_mere                ON animal (mere_id);
CREATE INDEX idx_animal_pere                ON animal (pere_id);
CREATE INDEX idx_animal_updated_at          ON animal (updated_at);

COMMENT ON COLUMN animal.identifiant IS 'Identifiant interne choisi par l''éleveur ou généré (ex. ZEB-2026-001).';
COMMENT ON COLUMN animal.boucle IS 'Numéro de boucle auriculaire, s''il y en a une.';
COMMENT ON COLUMN animal.marque_traditionnelle IS 'Marque d''oreille traditionnelle ou description de la marque.';
COMMENT ON COLUMN animal.date_naissance_estimee IS 'TRUE quand la date de naissance est approximative (fréquent en élevage extensif).';
COMMENT ON COLUMN animal.pere_id IS 'Peut référencer un animal d''une autre exploitation (reproducteur acheté ou emprunté).';

-- propriete : propriétaire, gardien (confiage), copropriétaire d'un animal
CREATE TABLE propriete (
    id               UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    animal_id        UUID         NOT NULL REFERENCES animal (id) ON DELETE CASCADE,
    eleveur_id       UUID         NOT NULL REFERENCES eleveur (id),
    type             VARCHAR(15)  NOT NULL,
    part_pourcentage NUMERIC(5,2) NOT NULL DEFAULT 100,
    date_debut       DATE         NOT NULL DEFAULT CURRENT_DATE,
    date_fin         DATE,
    conditions       TEXT,
    version          BIGINT       NOT NULL DEFAULT 0,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT ck_propriete_type  CHECK (type IN ('PROPRIETAIRE', 'GARDIEN', 'COPROPRIETAIRE')),
    CONSTRAINT ck_propriete_part  CHECK (part_pourcentage > 0 AND part_pourcentage <= 100),
    CONSTRAINT ck_propriete_dates CHECK (date_fin IS NULL OR date_fin >= date_debut)
);

-- Un seul propriétaire actif et un seul gardien actif par animal
CREATE UNIQUE INDEX uq_propriete_proprietaire_actif ON propriete (animal_id)
    WHERE type = 'PROPRIETAIRE' AND date_fin IS NULL;
CREATE UNIQUE INDEX uq_propriete_gardien_actif ON propriete (animal_id)
    WHERE type = 'GARDIEN' AND date_fin IS NULL;
CREATE INDEX idx_propriete_eleveur ON propriete (eleveur_id, type) WHERE date_fin IS NULL;

COMMENT ON TABLE propriete IS
    'Confiage : le propriétaire confie l''animal à un gardien (conditions = part des veaux ou des produits).';

-- Mise à jour automatique de updated_at
CREATE TRIGGER trg_lot_updated_at       BEFORE UPDATE ON lot       FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_animal_updated_at    BEFORE UPDATE ON animal    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_propriete_updated_at BEFORE UPDATE ON propriete FOR EACH ROW EXECUTE FUNCTION set_updated_at();
