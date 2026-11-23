-- Objectifs, finances, alertes et audit

-- RG-06 — Les pesées, productions et transactions servent à calculer la progression.
CREATE TABLE objectif (
    id                    UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    exploitation_id       UUID          NOT NULL REFERENCES exploitation (id),
    animal_id             UUID,
    lot_id                UUID,
    type                  VARCHAR(20)   NOT NULL,
    valeur_initiale       NUMERIC(14,3),
    valeur_cible          NUMERIC(14,3) NOT NULL,
    unite                 VARCHAR(20)   NOT NULL,
    date_debut            DATE          NOT NULL DEFAULT CURRENT_DATE,
    date_cible            DATE          NOT NULL,
    statut                VARCHAR(10)   NOT NULL DEFAULT 'EN_COURS',
    date_atteinte         DATE,
    courbe_reference_id   UUID          REFERENCES courbe_reference (id) ON DELETE SET NULL,
    notes                 TEXT,
    version               BIGINT        NOT NULL DEFAULT 0,
    created_at            TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ   NOT NULL DEFAULT now(),
    deleted_at            TIMESTAMPTZ,
    CONSTRAINT ck_objectif_type    CHECK (type IN ('POIDS_CIBLE', 'EFFECTIF_CIBLE', 'PRODUCTION_CIBLE', 'MARGE_CIBLE')),
    CONSTRAINT ck_objectif_unite   CHECK (unite IN ('KG', 'TETES', 'LITRE_PAR_JOUR', 'OEUFS_PAR_JOUR', 'POURCENT', 'ARIARY')),
    CONSTRAINT ck_objectif_statut  CHECK (statut IN ('EN_COURS', 'ATTEINT', 'ABANDONNE', 'ECHEC')),
    CONSTRAINT ck_objectif_valeur  CHECK (valeur_cible > 0),
    CONSTRAINT ck_objectif_dates   CHECK (date_cible >= date_debut),
    CONSTRAINT ck_objectif_atteinte CHECK ((statut = 'ATTEINT') = (date_atteinte IS NOT NULL)),
    -- Sans animal ni lot, l’objectif couvre toute l’exploitation.
    CONSTRAINT ck_objectif_cible   CHECK (num_nonnulls(animal_id, lot_id) <= 1),
    -- Un objectif de poids concerne forcément un animal ou un lot
    CONSTRAINT ck_objectif_poids   CHECK (type <> 'POIDS_CIBLE' OR num_nonnulls(animal_id, lot_id) = 1),
    CONSTRAINT fk_objectif_animal  FOREIGN KEY (animal_id, exploitation_id)
        REFERENCES animal (id, exploitation_id) ON UPDATE CASCADE,
    CONSTRAINT fk_objectif_lot     FOREIGN KEY (lot_id, exploitation_id)
        REFERENCES lot (id, exploitation_id) ON UPDATE CASCADE
);

CREATE INDEX idx_objectif_animal       ON objectif (animal_id) WHERE animal_id IS NOT NULL;
CREATE INDEX idx_objectif_lot          ON objectif (lot_id)    WHERE lot_id IS NOT NULL;
CREATE INDEX idx_objectif_exploitation ON objectif (exploitation_id, statut);
CREATE INDEX idx_objectif_date_cible   ON objectif (date_cible) WHERE statut = 'EN_COURS';
CREATE INDEX idx_objectif_updated_at   ON objectif (updated_at);

COMMENT ON COLUMN objectif.valeur_initiale IS 'Valeur de départ (ex. poids à la création de l''objectif) pour calculer le pourcentage de progression.';
COMMENT ON COLUMN objectif.courbe_reference_id IS 'Courbe de comparaison (en avance, dans la norme, en retard).';

-- Recettes et dépenses en ariary
CREATE TABLE transaction_financiere (
    id                UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    exploitation_id   UUID          NOT NULL REFERENCES exploitation (id),
    type              VARCHAR(10)   NOT NULL,
    categorie         VARCHAR(25)   NOT NULL,
    montant_ariary    NUMERIC(14,2) NOT NULL,
    date_transaction  DATE          NOT NULL,
    libelle           VARCHAR(200),
    mode_paiement     VARCHAR(15),
    evenement_id      UUID          REFERENCES evenement (id) ON DELETE SET NULL,
    tiers_id          UUID          REFERENCES tiers (id) ON DELETE SET NULL,
    animal_id         UUID          REFERENCES animal (id) ON DELETE SET NULL,
    lot_id            UUID          REFERENCES lot (id) ON DELETE SET NULL,
    saisi_par         UUID          REFERENCES utilisateur (id),
    notes             TEXT,
    version           BIGINT        NOT NULL DEFAULT 0,
    created_at        TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ   NOT NULL DEFAULT now(),
    deleted_at        TIMESTAMPTZ,
    CONSTRAINT ck_transaction_type    CHECK (type IN ('DEPENSE', 'RECETTE')),
    CONSTRAINT ck_transaction_montant CHECK (montant_ariary > 0),
    -- La catégorie doit correspondre au sens de la transaction
    CONSTRAINT ck_transaction_categorie CHECK (
        (type = 'DEPENSE' AND categorie IN ('ACHAT_ANIMAUX', 'ALIMENT', 'SANTE', 'MAIN_OEUVRE', 'BATIMENT',
                                            'TRANSPORT', 'TAXES', 'EQUIPEMENT', 'AUTRE_DEPENSE'))
        OR
        (type = 'RECETTE' AND categorie IN ('VENTE_ANIMAUX', 'VENTE_LAIT', 'VENTE_OEUFS', 'VENTE_FUMIER',
                                            'SUBVENTION', 'AUTRE_RECETTE'))
    ),
    CONSTRAINT ck_transaction_mode_paiement CHECK (mode_paiement IS NULL OR mode_paiement IN
        ('ESPECES', 'MVOLA', 'ORANGE_MONEY', 'AIRTEL_MONEY', 'VIREMENT', 'CHEQUE', 'CREDIT', 'AUTRE')),
    -- Sans animal ni lot, la transaction concerne toute l’exploitation.
    CONSTRAINT ck_transaction_cible CHECK (num_nonnulls(animal_id, lot_id) <= 1)
);

CREATE INDEX idx_transaction_exploitation_date ON transaction_financiere (exploitation_id, date_transaction DESC);
CREATE INDEX idx_transaction_categorie         ON transaction_financiere (exploitation_id, type, categorie);
CREATE INDEX idx_transaction_animal            ON transaction_financiere (animal_id)   WHERE animal_id IS NOT NULL;
CREATE INDEX idx_transaction_lot               ON transaction_financiere (lot_id)      WHERE lot_id IS NOT NULL;
CREATE INDEX idx_transaction_evenement         ON transaction_financiere (evenement_id) WHERE evenement_id IS NOT NULL;
CREATE INDEX idx_transaction_updated_at        ON transaction_financiere (updated_at);

COMMENT ON TABLE transaction_financiere IS
    'RG-12 : visible uniquement par le propriétaire de l''exploitation (et les membres explicitement autorisés).';

-- alerte : rappels générés chaque jour par la tâche planifiée
-- La clé de déduplication évite de recréer une alerte pour la même échéance.
CREATE TABLE alerte (
    id                  UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    utilisateur_id      UUID         NOT NULL REFERENCES utilisateur (id) ON DELETE CASCADE,
    exploitation_id     UUID         REFERENCES exploitation (id) ON DELETE CASCADE,
    type                VARCHAR(25)  NOT NULL,
    date_echeance       DATE         NOT NULL,
    statut              VARCHAR(10)  NOT NULL DEFAULT 'EN_ATTENTE',
    message             VARCHAR(500) NOT NULL,
    cle_deduplication   VARCHAR(200) NOT NULL,
    animal_id           UUID         REFERENCES animal (id) ON DELETE CASCADE,
    lot_id              UUID         REFERENCES lot (id) ON DELETE CASCADE,
    evenement_id        UUID         REFERENCES evenement (id) ON DELETE SET NULL,
    objectif_id         UUID         REFERENCES objectif (id) ON DELETE CASCADE,
    envoyee_at          TIMESTAMPTZ,
    traitee_at          TIMESTAMPTZ,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT ck_alerte_type   CHECK (type IN ('VACCIN_A_FAIRE', 'RAPPEL_VACCIN', 'MISE_BAS_PREVUE', 'PESEE_EN_RETARD',
                                                'DELAI_ATTENTE', 'OBJECTIF_EN_RETARD', 'OBJECTIF_ATTEINT', 'AUTRE')),
    CONSTRAINT ck_alerte_statut CHECK (statut IN ('EN_ATTENTE', 'ENVOYEE', 'VUE', 'TRAITEE', 'IGNOREE')),
    CONSTRAINT uq_alerte_deduplication UNIQUE (utilisateur_id, cle_deduplication)
);

CREATE INDEX idx_alerte_utilisateur_statut ON alerte (utilisateur_id, statut, date_echeance);
CREATE INDEX idx_alerte_echeance_attente   ON alerte (date_echeance) WHERE statut = 'EN_ATTENTE';
CREATE INDEX idx_alerte_animal             ON alerte (animal_id) WHERE animal_id IS NOT NULL;
CREATE INDEX idx_alerte_lot                ON alerte (lot_id)    WHERE lot_id IS NOT NULL;

-- Historique des actions sensibles et des échecs de connexion
CREATE TABLE journal_audit (
    id              BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    utilisateur_id  UUID,
    action          VARCHAR(60)  NOT NULL,
    entite          VARCHAR(60),
    entite_id       UUID,
    details         JSONB,
    adresse_ip      VARCHAR(45),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_audit_utilisateur ON journal_audit (utilisateur_id, created_at DESC);
CREATE INDEX idx_audit_entite      ON journal_audit (entite, entite_id);
CREATE INDEX idx_audit_date        ON journal_audit (created_at DESC);

-- Mise à jour automatique de updated_at
CREATE TRIGGER trg_objectif_updated_at              BEFORE UPDATE ON objectif              FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_transaction_financiere_updated_at BEFORE UPDATE ON transaction_financiere FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_alerte_updated_at                BEFORE UPDATE ON alerte                FOR EACH ROW EXECUTE FUNCTION set_updated_at();
