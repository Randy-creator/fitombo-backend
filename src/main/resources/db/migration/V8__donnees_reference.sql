-- Données de référence initiales
-- Un vétérinaire ou un technicien doit valider ces valeurs avant leur diffusion.
-- Faire relire les noms malagasy par un locuteur natif. Les traductions incertaines restent vides.
-- Après application de V8, corriger les valeurs dans une nouvelle migration.

-- Espèces
INSERT INTO espece (code, nom_fr, nom_mg, mode_suivi, duree_gestation_jours, duree_incubation_jours, frequence_pesee_jours, rendement_carcasse_pct) VALUES
    ('ZEBU',          'Zébu',                  'Omby',   'INDIVIDUEL', 285,  NULL, 30, 50.00),
    ('BOVIN_LAITIER', 'Bovin laitier',         'Omby',   'INDIVIDUEL', 283,  NULL, 30, 48.00),
    ('PORC',          'Porc',                  'Kisoa',  'MIXTE',      114,  NULL, 14, 77.00),
    ('POULET_CHAIR',  'Poulet de chair',       'Akoho',  'LOT',        NULL, 21,   7,  70.00),
    ('POULET_GASY',   'Poulet gasy (rustique)','Akoho gasy', 'LOT',    NULL, 21,   30, 68.00),
    ('PONDEUSE',      'Poule pondeuse',        'Akoho',  'LOT',        NULL, 21,   28, NULL),
    ('CANARD',        'Canard',                NULL,     'LOT',        NULL, 28,   14, 65.00),
    ('LAPIN',         'Lapin',                 NULL,     'LOT',        31,   NULL, 14, 58.00),
    ('CHEVRE',        'Chèvre',                'Osy',    'INDIVIDUEL', 150,  NULL, 30, 48.00),
    ('MOUTON',        'Mouton',                'Ondry',  'INDIVIDUEL', 150,  NULL, 30, 48.00),
    ('TILAPIA',       'Tilapia',               'Trondro','LOT',        NULL, NULL, 30, NULL);

-- Races
INSERT INTO race (espece_code, nom, origine) VALUES
    ('ZEBU',          'Zébu malgache local',                    'Madagascar'),
    ('ZEBU',          'Zébu croisé (Brahman)',                  'Croisement'),

    ('BOVIN_LAITIER', 'Pie Rouge Norvégienne',                  'Norvège'),
    ('BOVIN_LAITIER', 'Holstein',                               'Pays-Bas / Allemagne'),
    ('BOVIN_LAITIER', 'Normande',                               'France'),
    ('BOVIN_LAITIER', 'Croisé laitier',                         'Croisement'),

    ('PORC',          'Large White',                            'Royaume-Uni'),
    ('PORC',          'Landrace',                               'Danemark'),
    ('PORC',          'Duroc',                                  'États-Unis'),
    ('PORC',          'Porc local (kisoa gasy)',                'Madagascar'),
    ('PORC',          'Croisé',                                 'Croisement'),

    ('POULET_CHAIR',  'Cobb 500',                               'Sélection industrielle'),
    ('POULET_CHAIR',  'Ross 308',                               'Sélection industrielle'),
    ('POULET_CHAIR',  'Hubbard',                                'Sélection industrielle'),
    ('POULET_CHAIR',  'Sasso',                                  'France (croissance lente)'),

    ('POULET_GASY',   'Poulet gasy (local)',                    'Madagascar'),
    ('POULET_GASY',   'Gasy amélioré (croisé)',                 'Croisement'),

    ('PONDEUSE',      'ISA Brown',                              'Sélection industrielle'),
    ('PONDEUSE',      'Lohmann Brown',                          'Sélection industrielle'),
    ('PONDEUSE',      'Hy-Line Brown',                          'Sélection industrielle'),

    ('CANARD',        'Canard local',                           'Madagascar'),
    ('CANARD',        'Canard de Barbarie',                     'Amérique du Sud'),
    ('CANARD',        'Canard Pékin',                           'Chine'),

    ('LAPIN',         'Néo-Zélandais blanc',                    'États-Unis'),
    ('LAPIN',         'Californien',                            'États-Unis'),
    ('LAPIN',         'Géant des Flandres',                     'Belgique'),
    ('LAPIN',         'Lapin local',                            'Madagascar'),

    ('CHEVRE',        'Chèvre locale',                          'Madagascar'),
    ('CHEVRE',        'Boer',                                   'Afrique du Sud'),
    ('CHEVRE',        'Saanen',                                 'Suisse'),
    ('CHEVRE',        'Croisée',                                'Croisement'),

    ('MOUTON',        'Mouton local',                           'Madagascar'),
    ('MOUTON',        'Dorper',                                 'Afrique du Sud'),
    ('MOUTON',        'Croisé',                                 'Croisement'),

    ('TILAPIA',       'Tilapia du Nil (Oreochromis niloticus)', 'Afrique'),
    ('TILAPIA',       'Oreochromis mossambicus',                'Afrique australe');

-- Courbes de croissance (kg par âge en jours)
-- Les bornes appliquent une tolérance en pourcentage au poids attendu.
CREATE TEMP TABLE tmp_profil (
    cle        VARCHAR(40)   PRIMARY KEY,
    sexe       VARCHAR(10)   NOT NULL,
    libelle    VARCHAR(150)  NOT NULL,
    tolerance  NUMERIC(5,2)  NOT NULL
) ON COMMIT DROP;

CREATE TEMP TABLE tmp_profil_race (
    cle         VARCHAR(40)  NOT NULL,
    espece_code VARCHAR(30)  NOT NULL,
    race_nom    VARCHAR(120) NOT NULL
) ON COMMIT DROP;

CREATE TEMP TABLE tmp_profil_point (
    cle       VARCHAR(40)  NOT NULL,
    age_jours INTEGER      NOT NULL,
    poids_kg  NUMERIC(9,3) NOT NULL
) ON COMMIT DROP;

INSERT INTO tmp_profil (cle, sexe, libelle, tolerance) VALUES
    ('ZEBU_LOCAL_M',       'MALE',    'Zébu malgache mâle, élevage extensif (indicatif)',     20),
    ('ZEBU_LOCAL_F',       'FEMELLE', 'Zébu malgache femelle, élevage extensif (indicatif)',  20),
    ('GENISSE_LAITIERE',   'FEMELLE', 'Génisse laitière (indicatif)',                         12),
    ('POULET_CHAIR_STD',   'MIXTE',   'Poulet de chair standard (indicatif)',                 12),
    ('POULET_SASSO',       'MIXTE',   'Poulet à croissance lente, type Sasso (indicatif)',    15),
    ('POULET_GASY_M',      'MALE',    'Poulet gasy mâle (indicatif)',                         20),
    ('POULET_GASY_F',      'FEMELLE', 'Poulet gasy femelle (indicatif)',                      20),
    ('PONDEUSE',           'FEMELLE', 'Poule pondeuse, poids corporel (indicatif)',           10),
    ('PORC_ENGRAISSEMENT', 'MIXTE',   'Porc d''engraissement (indicatif)',                    15),
    ('LAPIN',              'MIXTE',   'Lapin d''engraissement (indicatif)',                   15),
    ('TILAPIA',            'MIXTE',   'Tilapia en grossissement (indicatif)',                 25),
    ('CHEVRE',             'MIXTE',   'Chèvre (indicatif)',                                   20),
    ('MOUTON',             'MIXTE',   'Mouton (indicatif)',                                   20);

INSERT INTO tmp_profil_race (cle, espece_code, race_nom) VALUES
    ('ZEBU_LOCAL_M',       'ZEBU',          'Zébu malgache local'),
    ('ZEBU_LOCAL_F',       'ZEBU',          'Zébu malgache local'),
    ('GENISSE_LAITIERE',   'BOVIN_LAITIER', 'Pie Rouge Norvégienne'),
    ('GENISSE_LAITIERE',   'BOVIN_LAITIER', 'Holstein'),
    ('GENISSE_LAITIERE',   'BOVIN_LAITIER', 'Normande'),
    ('GENISSE_LAITIERE',   'BOVIN_LAITIER', 'Croisé laitier'),
    ('POULET_CHAIR_STD',   'POULET_CHAIR',  'Cobb 500'),
    ('POULET_CHAIR_STD',   'POULET_CHAIR',  'Ross 308'),
    ('POULET_CHAIR_STD',   'POULET_CHAIR',  'Hubbard'),
    ('POULET_SASSO',       'POULET_CHAIR',  'Sasso'),
    ('POULET_GASY_M',      'POULET_GASY',   'Poulet gasy (local)'),
    ('POULET_GASY_M',      'POULET_GASY',   'Gasy amélioré (croisé)'),
    ('POULET_GASY_F',      'POULET_GASY',   'Poulet gasy (local)'),
    ('POULET_GASY_F',      'POULET_GASY',   'Gasy amélioré (croisé)'),
    ('PONDEUSE',           'PONDEUSE',      'ISA Brown'),
    ('PONDEUSE',           'PONDEUSE',      'Lohmann Brown'),
    ('PONDEUSE',           'PONDEUSE',      'Hy-Line Brown'),
    ('PORC_ENGRAISSEMENT', 'PORC',          'Large White'),
    ('PORC_ENGRAISSEMENT', 'PORC',          'Landrace'),
    ('PORC_ENGRAISSEMENT', 'PORC',          'Croisé'),
    ('LAPIN',              'LAPIN',         'Néo-Zélandais blanc'),
    ('LAPIN',              'LAPIN',         'Californien'),
    ('TILAPIA',            'TILAPIA',       'Tilapia du Nil (Oreochromis niloticus)'),
    ('CHEVRE',             'CHEVRE',        'Chèvre locale'),
    ('CHEVRE',             'CHEVRE',        'Croisée'),
    ('MOUTON',             'MOUTON',        'Mouton local');

INSERT INTO tmp_profil_point (cle, age_jours, poids_kg) VALUES
    -- Zébu malgache mâle, extensif
    ('ZEBU_LOCAL_M', 0, 18),   ('ZEBU_LOCAL_M', 90, 55),   ('ZEBU_LOCAL_M', 180, 95),
    ('ZEBU_LOCAL_M', 365, 145), ('ZEBU_LOCAL_M', 545, 190), ('ZEBU_LOCAL_M', 730, 240),
    ('ZEBU_LOCAL_M', 1095, 315), ('ZEBU_LOCAL_M', 1460, 370), ('ZEBU_LOCAL_M', 1825, 405),
    -- Zébu malgache femelle, extensif
    ('ZEBU_LOCAL_F', 0, 17),   ('ZEBU_LOCAL_F', 90, 52),   ('ZEBU_LOCAL_F', 180, 90),
    ('ZEBU_LOCAL_F', 365, 135), ('ZEBU_LOCAL_F', 545, 175), ('ZEBU_LOCAL_F', 730, 220),
    ('ZEBU_LOCAL_F', 1095, 280), ('ZEBU_LOCAL_F', 1460, 320), ('ZEBU_LOCAL_F', 1825, 345),
    -- Génisse laitière
    ('GENISSE_LAITIERE', 0, 40),   ('GENISSE_LAITIERE', 90, 95),   ('GENISSE_LAITIERE', 180, 170),
    ('GENISSE_LAITIERE', 365, 300), ('GENISSE_LAITIERE', 540, 410), ('GENISSE_LAITIERE', 730, 520),
    ('GENISSE_LAITIERE', 1095, 580),
    -- Poulet de chair standard
    ('POULET_CHAIR_STD', 0, 0.042), ('POULET_CHAIR_STD', 7, 0.18),  ('POULET_CHAIR_STD', 14, 0.45),
    ('POULET_CHAIR_STD', 21, 0.88), ('POULET_CHAIR_STD', 28, 1.43), ('POULET_CHAIR_STD', 35, 2.04),
    ('POULET_CHAIR_STD', 42, 2.65),
    -- Poulet à croissance lente (Sasso)
    ('POULET_SASSO', 0, 0.04),  ('POULET_SASSO', 14, 0.28), ('POULET_SASSO', 28, 0.68),
    ('POULET_SASSO', 42, 1.15), ('POULET_SASSO', 56, 1.65), ('POULET_SASSO', 70, 2.10),
    ('POULET_SASSO', 84, 2.50),
    -- Poulet gasy mâle
    ('POULET_GASY_M', 0, 0.035), ('POULET_GASY_M', 30, 0.16), ('POULET_GASY_M', 60, 0.38),
    ('POULET_GASY_M', 90, 0.68), ('POULET_GASY_M', 120, 1.00), ('POULET_GASY_M', 150, 1.35),
    ('POULET_GASY_M', 180, 1.70),
    -- Poulet gasy femelle
    ('POULET_GASY_F', 0, 0.033), ('POULET_GASY_F', 30, 0.14), ('POULET_GASY_F', 60, 0.32),
    ('POULET_GASY_F', 90, 0.55), ('POULET_GASY_F', 120, 0.80), ('POULET_GASY_F', 150, 1.05),
    ('POULET_GASY_F', 180, 1.25),
    -- Poule pondeuse
    ('PONDEUSE', 0, 0.04),   ('PONDEUSE', 28, 0.35),  ('PONDEUSE', 56, 0.78),
    ('PONDEUSE', 84, 1.18),  ('PONDEUSE', 112, 1.50), ('PONDEUSE', 140, 1.70),
    ('PONDEUSE', 175, 1.85), ('PONDEUSE', 280, 1.95), ('PONDEUSE', 420, 2.00),
    -- Porc d'engraissement
    ('PORC_ENGRAISSEMENT', 0, 1.4),   ('PORC_ENGRAISSEMENT', 28, 7.5),   ('PORC_ENGRAISSEMENT', 56, 18),
    ('PORC_ENGRAISSEMENT', 84, 33),   ('PORC_ENGRAISSEMENT', 112, 52),   ('PORC_ENGRAISSEMENT', 140, 72),
    ('PORC_ENGRAISSEMENT', 168, 93),  ('PORC_ENGRAISSEMENT', 196, 112),  ('PORC_ENGRAISSEMENT', 224, 128),
    -- Lapin d'engraissement
    ('LAPIN', 0, 0.05),  ('LAPIN', 28, 0.55), ('LAPIN', 42, 1.05), ('LAPIN', 56, 1.60),
    ('LAPIN', 70, 2.15), ('LAPIN', 84, 2.60), ('LAPIN', 112, 3.20),
    -- Tilapia
    ('TILAPIA', 0, 0.002), ('TILAPIA', 30, 0.008), ('TILAPIA', 60, 0.04),  ('TILAPIA', 90, 0.11),
    ('TILAPIA', 120, 0.20), ('TILAPIA', 150, 0.30), ('TILAPIA', 180, 0.40), ('TILAPIA', 210, 0.50),
    -- Chèvre
    ('CHEVRE', 0, 2.5), ('CHEVRE', 90, 10), ('CHEVRE', 180, 17), ('CHEVRE', 365, 25), ('CHEVRE', 540, 30),
    -- Mouton
    ('MOUTON', 0, 3), ('MOUTON', 90, 12), ('MOUTON', 180, 20), ('MOUTON', 365, 30), ('MOUTON', 540, 35);

INSERT INTO courbe_reference (race_id, sexe, libelle, source, valide, actif)
SELECT r.id, p.sexe, p.libelle,
       'Valeurs indicatives de départ, à faire valider par un vétérinaire ou un technicien d''élevage avant usage.',
       FALSE, TRUE
FROM tmp_profil p
JOIN tmp_profil_race pr ON pr.cle = p.cle
JOIN race r             ON r.espece_code = pr.espece_code AND r.nom = pr.race_nom;

INSERT INTO point_courbe (courbe_id, age_jours, poids_attendu_kg, poids_min_kg, poids_max_kg)
SELECT c.id,
       pt.age_jours,
       pt.poids_kg,
       ROUND(pt.poids_kg * (1 - p.tolerance / 100.0), 3),
       ROUND(pt.poids_kg * (1 + p.tolerance / 100.0), 3)
FROM courbe_reference c
JOIN tmp_profil p        ON p.libelle = c.libelle AND p.sexe = c.sexe
JOIN tmp_profil_point pt ON pt.cle = p.cle;

-- Calendriers préventifs indicatifs, à confirmer avec un vétérinaire.
CREATE TEMP TABLE tmp_protocole (
    espece_code VARCHAR(30)  NOT NULL,
    nom         VARCHAR(150) NOT NULL,
    description TEXT
) ON COMMIT DROP;

CREATE TEMP TABLE tmp_etape (
    espece_code        VARCHAR(30)  NOT NULL,
    nom                VARCHAR(150) NOT NULL,
    ordre              SMALLINT     NOT NULL,
    age_jours          INTEGER      NOT NULL,
    type_acte          VARCHAR(25)  NOT NULL,
    produit            VARCHAR(200) NOT NULL,
    maladie_ciblee     VARCHAR(150),
    rappel_apres_jours INTEGER
) ON COMMIT DROP;

INSERT INTO tmp_protocole (espece_code, nom, description) VALUES
    ('POULET_CHAIR', 'Calendrier sanitaire poulet de chair (indicatif)',
        'Vaccination de base. À adapter à la pression sanitaire locale et à l''avis du vétérinaire.'),
    ('POULET_GASY',  'Calendrier sanitaire poulet gasy (indicatif)',
        'Vaccination de base des poulets villageois. Rappel de la Newcastle tous les 3 mois environ.'),
    ('PONDEUSE',     'Calendrier sanitaire poule pondeuse (indicatif)',
        'Programme de démarrage et d''élevage avant la ponte. À compléter avec un vétérinaire pour la phase de ponte.'),
    ('PORC',         'Calendrier sanitaire porcin (indicatif)',
        'Il n''existe pas de vaccin contre la peste porcine africaine (PPA) : seule la biosécurité protège (isolement, désinfection, pas de déchets de cuisine non cuits, signalement immédiat de toute mortalité suspecte).'),
    ('ZEBU',         'Calendrier sanitaire bovin (indicatif)',
        'Vaccinations et déparasitage de base. À adapter à la zone et à l''avis du vétérinaire sanitaire.'),
    ('BOVIN_LAITIER','Calendrier sanitaire bovin (indicatif)',
        'Vaccinations et déparasitage de base. À adapter à la zone et à l''avis du vétérinaire sanitaire.'),
    ('CHEVRE',       'Calendrier sanitaire petits ruminants (indicatif)',
        'Déparasitage et vaccination de base. À adapter à la zone et à l''avis du vétérinaire.'),
    ('MOUTON',       'Calendrier sanitaire petits ruminants (indicatif)',
        'Déparasitage et vaccination de base. À adapter à la zone et à l''avis du vétérinaire.');

INSERT INTO tmp_etape (espece_code, nom, ordre, age_jours, type_acte, produit, maladie_ciblee, rappel_apres_jours) VALUES
    -- Poulet de chair
    ('POULET_CHAIR', 'Calendrier sanitaire poulet de chair (indicatif)', 1,   7, 'VACCINATION', 'Vaccin Newcastle (HB1)',        'Maladie de Newcastle', NULL),
    ('POULET_CHAIR', 'Calendrier sanitaire poulet de chair (indicatif)', 2,  14, 'VACCINATION', 'Vaccin Gumboro (IBD)',         'Maladie de Gumboro',   NULL),
    ('POULET_CHAIR', 'Calendrier sanitaire poulet de chair (indicatif)', 3,  21, 'VACCINATION', 'Vaccin Newcastle (La Sota)',   'Maladie de Newcastle', NULL),
    -- Poulet gasy
    ('POULET_GASY',  'Calendrier sanitaire poulet gasy (indicatif)',     1,   7, 'VACCINATION', 'Vaccin Newcastle thermostable (I-2)', 'Maladie de Newcastle', NULL),
    ('POULET_GASY',  'Calendrier sanitaire poulet gasy (indicatif)',     2,  21, 'VACCINATION', 'Vaccin Gumboro (IBD)',        'Maladie de Gumboro',   NULL),
    ('POULET_GASY',  'Calendrier sanitaire poulet gasy (indicatif)',     3,  35, 'VACCINATION', 'Vaccin Newcastle (rappel)',   'Maladie de Newcastle', 90),
    -- Poule pondeuse
    ('PONDEUSE',     'Calendrier sanitaire poule pondeuse (indicatif)',  1,   7, 'VACCINATION', 'Vaccin Newcastle + bronchite infectieuse', 'Newcastle, bronchite infectieuse', NULL),
    ('PONDEUSE',     'Calendrier sanitaire poule pondeuse (indicatif)',  2,  14, 'VACCINATION', 'Vaccin Gumboro (IBD)',        'Maladie de Gumboro',   NULL),
    ('PONDEUSE',     'Calendrier sanitaire poule pondeuse (indicatif)',  3,  28, 'VACCINATION', 'Vaccin Newcastle (rappel)',   'Maladie de Newcastle', NULL),
    ('PONDEUSE',     'Calendrier sanitaire poule pondeuse (indicatif)',  4,  56, 'VACCINATION', 'Vaccin variole aviaire',      'Variole aviaire',      NULL),
    ('PONDEUSE',     'Calendrier sanitaire poule pondeuse (indicatif)',  5, 112, 'VACCINATION', 'Vaccin Newcastle + syndrome chute de ponte', 'Newcastle, syndrome chute de ponte', NULL),
    -- Porc
    ('PORC',         'Calendrier sanitaire porcin (indicatif)',          1,   3, 'TRAITEMENT_PREVENTIF', 'Fer injectable',                    'Anémie du porcelet', NULL),
    ('PORC',         'Calendrier sanitaire porcin (indicatif)',          2,  28, 'DEPARASITAGE', 'Antiparasitaire (ivermectine)',    'Parasites internes et externes', 90),
    ('PORC',         'Calendrier sanitaire porcin (indicatif)',          3,  60, 'VACCINATION',  'Vaccin rouget (érysipèle)',        'Rouget du porc',     180),
    -- Petits ruminants
    ('CHEVRE',       'Calendrier sanitaire petits ruminants (indicatif)', 1,  60, 'DEPARASITAGE', 'Antiparasitaire interne',            'Vers gastro-intestinaux', 90),
    ('CHEVRE',       'Calendrier sanitaire petits ruminants (indicatif)', 2, 120, 'VACCINATION',  'Vaccin charbon bactéridien',        'Charbon bactéridien',  365),
    ('MOUTON',       'Calendrier sanitaire petits ruminants (indicatif)', 1,  60, 'DEPARASITAGE', 'Antiparasitaire interne',            'Vers gastro-intestinaux', 90),
    ('MOUTON',       'Calendrier sanitaire petits ruminants (indicatif)', 2, 120, 'VACCINATION',  'Vaccin charbon bactéridien',        'Charbon bactéridien',  365);

-- Protocole bovin identique pour le zébu et le bovin laitier
INSERT INTO tmp_etape (espece_code, nom, ordre, age_jours, type_acte, produit, maladie_ciblee, rappel_apres_jours)
SELECT e.code, 'Calendrier sanitaire bovin (indicatif)', s.ordre, s.age_jours, s.type_acte, s.produit, s.maladie_ciblee, s.rappel
FROM (VALUES ('ZEBU'), ('BOVIN_LAITIER')) AS e(code)
CROSS JOIN (VALUES
    (1, 120, 'VACCINATION', 'Vaccin charbon symptomatique',                         'Charbon symptomatique',  365),
    (2, 150, 'VACCINATION', 'Vaccin charbon bactéridien',                           'Charbon bactéridien',    365),
    (3, 180, 'DEPARASITAGE', 'Antiparasitaire interne (ivermectine, albendazole)', 'Parasites internes',     180)
) AS s(ordre, age_jours, type_acte, produit, maladie_ciblee, rappel);

INSERT INTO protocole_sanitaire (espece_code, nom, description, source, valide, actif)
SELECT p.espece_code, p.nom, p.description,
       'Calendrier indicatif de départ, à faire valider par un vétérinaire avant usage. Ne remplace pas l''avis d''un vétérinaire.',
       FALSE, TRUE
FROM tmp_protocole p;

INSERT INTO etape_protocole (protocole_id, ordre, age_jours, type_acte, produit, maladie_ciblee, rappel_apres_jours)
SELECT ps.id, e.ordre, e.age_jours, e.type_acte, e.produit, e.maladie_ciblee, e.rappel_apres_jours
FROM tmp_etape e
JOIN protocole_sanitaire ps ON ps.espece_code = e.espece_code AND ps.nom = e.nom;
