# Tester les lectures Fitombo en local

## Démarrer sur une base dédiée

Depuis la racine du projet, démarrer PostgreSQL puis créer une base vide une seule fois.
Cette base évite le conflit de checksums si les anciennes migrations sont déjà
enregistrées dans la base habituelle.

```bash
docker compose up -d postgres
docker compose exec -T postgres sh -c 'createdb -U "$POSTGRES_USER" fitombo_api_test'
SPRING_DOCKER_COMPOSE_ENABLED=false \
  DB_URL=jdbc:postgresql://127.0.0.1:5433/fitombo_api_test \
  ./mvnw spring-boot:run
```

Adapter le port à `DB_PORT` si nécessaire. Après le démarrage, dans un autre terminal,
charger les données d’exemple une seule fois.

```bash
docker compose exec -T postgres sh -c \
  'psql -U "$POSTGRES_USER" -d fitombo_api_test -v ON_ERROR_STOP=1 --single-transaction' \
  < src/test/resources/sql/exploitations-api.sql
```

## Se connecter dans Postman

Sur la base de démonstration, attribuer un mot de passe au membre déjà chargé et
marquer son téléphone comme vérifié. Ces identifiants sont réservés au test local.

```bash
docker compose exec -T postgres sh -c \
  'psql -U "$POSTGRES_USER" -d fitombo_api_test -v ON_ERROR_STOP=1 --single-transaction' <<'SQL'
CREATE EXTENSION IF NOT EXISTS pgcrypto;
UPDATE utilisateur
SET telephone_verifie = true,
    mot_de_passe_hash = crypt('Fitombo-Demo-2026!', gen_salt('bf', 10))
WHERE id = '10000000-0000-4000-8000-000000000002';
SQL
```

Dans Postman, créer une requête **POST** vers `{{baseUrl}}/auth/connexion`.
Sélectionner **No Auth**, puis **Body → raw → JSON** et envoyer ce corps.

```json
{
  "telephone": "+261340001002",
  "motDePasse": "Fitombo-Demo-2026!"
}
```

La réponse `200` fournit `accessToken`, `refreshToken`, `tokenType` et `expiresIn` (1 800 secondes
par défaut). Copier `accessToken` dans **Authorization → Bearer Token** de la
collection. Laisser les requêtes de lecture hériter de cette autorisation.
Un mauvais mot de passe renvoie `401` et un téléphone mal formé renvoie `400`.
Au-delà de dix tentatives par minute depuis une même adresse, le login renvoie `429`.

## Générer un jeton de test depuis le terminal

Ce jeton de quinze minutes sert aux tests isolés qui ne passent pas par le login. Le script lit la clé du `.env` local sans l’afficher.
Il utilise par défaut l’utilisateur membre de la ferme de démonstration.

```bash
TOKEN=$(python3 - <<'PY'
import base64
import hashlib
import hmac
import json
import time
from pathlib import Path

def b64(value):
    return base64.urlsafe_b64encode(value).rstrip(b"=")

secret = next(line.partition("=")[2] for line in Path(".env").read_text().splitlines()
              if line.startswith("JWT_SECRET="))
now = int(time.time())
header = b64(json.dumps({"alg": "HS256", "typ": "JWT"}).encode())
claims = b64(json.dumps({
    "iss": "fitombo", "sub": "10000000-0000-4000-8000-000000000002",
    "role": "ELEVEUR", "token_use": "access", "iat": now, "exp": now + 900
}).encode())
message = header + b"." + claims
signature = b64(hmac.new(secret.encode(), message, hashlib.sha256).digest())
print((message + b"." + signature).decode())
PY
)
```

## Appeler les routes

```bash
BASE=http://127.0.0.1:8080
FERME=30000000-0000-4000-8000-000000000001
PRIVEE=30000000-0000-4000-8000-000000000002

curl -i -H "Authorization: Bearer $TOKEN" "$BASE/especes?page=0&size=5"
curl -i -H "Authorization: Bearer $TOKEN" "$BASE/especes/ZEBU/races"
curl -i -H "Authorization: Bearer $TOKEN" "$BASE/exploitations"
curl -i -H "Authorization: Bearer $TOKEN" "$BASE/exploitations/$FERME"
curl -i -H "Authorization: Bearer $TOKEN" "$BASE/exploitations/$FERME/animaux?statut=VENDU"
curl -i -H "Authorization: Bearer $TOKEN" "$BASE/exploitations/$PRIVEE/animaux"
curl -i "$BASE/especes"
```

Les cinq premières requêtes renvoient `200`. La liste des exploitations contient
uniquement **Ferme API**. Le filtre `VENDU` renvoie **API-VENDU**. La ferme privée
renvoie `404` pour ce membre dont l’accès a expiré. L’appel sans jeton renvoie `401`.

Les mêmes routes sont disponibles dans Swagger UI en développement et peuvent être
appelées depuis Postman en sélectionnant **Bearer Token**.

## Tester le profil, les emplacements, les lots et les alertes

Après la connexion du membre de démonstration, utilise le même `accessToken`
et l’identifiant de **Ferme API** `30000000-0000-4000-8000-000000000001`.

| Méthode et route | Données et résultat attendu |
| --- | --- |
| `GET /me` | `200` et le profil du membre connecté. |
| `PUT /me` | Avec `{"nom":"Membre API","langue":"fr"}`, `200` et le profil mis à jour. Les langues acceptées sont `fr` et `mg`. |
| `GET /exploitations/{id}/emplacements` | `200` et une liste paginée. |
| `GET /animaux/{id}` | Utiliser un `id` obtenu dans la liste des animaux de la ferme. La fiche renvoie `200`, y compris pour un animal vendu. |
| `GET /exploitations/{id}/lots?statut=ACTIF` | `200` et les lots actifs de la ferme. Le filtre est facultatif. |
| `GET /alertes?statut=EN_ATTENTE` | `200` et les alertes en attente du membre qui sont dans son périmètre. |
| `PATCH /alertes/{id}` | Avec `{"statut":"VUE"}`, `204`. La répétition de la requête renvoie aussi `204`. |

Tu peux ajouter ces exemples dans **la base de test** depuis l’éditeur SQL de
DBeaver, après avoir chargé le jeu de données de démonstration.

```sql
INSERT INTO emplacement (exploitation_id, nom, type, capacite)
VALUES ('30000000-0000-4000-8000-000000000001', 'Enclos de démonstration', 'ENCLOS', 20)
ON CONFLICT DO NOTHING;

INSERT INTO lot (exploitation_id, espece_code, code, effectif_initial, effectif_actuel)
VALUES ('30000000-0000-4000-8000-000000000001', 'PORC', 'LOT-DEMO', 20, 18)
ON CONFLICT DO NOTHING;

INSERT INTO alerte (utilisateur_id, exploitation_id, type, date_echeance, message, cle_deduplication)
VALUES ('10000000-0000-4000-8000-000000000002', '30000000-0000-4000-8000-000000000001',
        'AUTRE', current_date, 'Alerte de démonstration', 'alerte-demo-postman')
ON CONFLICT DO NOTHING;
```

Récupère l’identifiant de l’alerte dans la réponse de `GET /alertes` avant de
faire le `PATCH`. Ensuite, filtre avec `statut=VUE` pour vérifier le changement.
Teste aussi les emplacements et les lots avec l’identifiant de la ferme privée
`30000000-0000-4000-8000-000000000002`. Le membre de démonstration doit recevoir `404`.

## Créer une exploitation, un emplacement et un lot

Pour ces requêtes, utilise le compte propriétaire de **Ferme API**
(`+261340001001`). Dans la préparation du login de démonstration, utilise
l’identifiant `10000000-0000-4000-8000-000000000001` pour lui attribuer le mot
de passe de test, puis utilise le jeton obtenu avec ce numéro.

| Route | Corps JSON de démonstration |
| --- | --- |
| `POST /exploitations` | `{"nom":"Nouvelle ferme","systeme":"EXTENSIF","commune":"Antsirabe"}` |
| `POST /exploitations/{id}/emplacements` | `{"nom":"Parc A","type":"PARC","capacite":20}` |
| `POST /exploitations/{id}/lots` | `{"code":"LOT-A","especeCode":"PORC","dateCreation":"2026-01-01","effectifInitial":18}` |

Après chaque `201`, récupère l’identifiant de la réponse. Utilise l’identifiant
de l’exploitation dans les deux dernières routes. Tu peux aussi indiquer
`emplacementId` dans la création du lot et `raceId` obtenu par
`GET /especes/PORC/races`. Le lot démarre avec le statut `ACTIF` et un
effectif actuel égal à son effectif initial.

Pour vérifier les refus, renvoie la même création avec le même propriétaire
(`409`), essaie un effectif nul (`400`) ou crée un emplacement dans la ferme
avec le jeton du membre lecteur (`404`).

## Enregistrer, rechercher et peser le cheptel

Utilise le jeton du propriétaire pour les écritures. Les membres actifs peuvent
consulter le cheptel et l’historique de leur exploitation.

1. Envoie `POST /exploitations/{id}/animaux` avec le corps ci-dessous. La réponse
   `201` et l’en-tête `Location` donnent l’identifiant de l’animal.
2. Vérifie avec `GET /exploitations/{id}/animaux?especeCode=PORC&sexe=FEMELLE&recherche=TRUIE`.
   Les filtres facultatifs sont `recherche`, `especeCode`, `emplacementId`, `sexe`,
   `statut`, `ageMinJours` et `ageMaxJours`. Les animaux sans date de naissance
   sont exclus lorsqu’une borne d’âge est utilisée.
3. Sur un lot actif de la même espèce et de la même exploitation, appelle
   `POST /lots/{lotId}/animaux` avec `{"animalId":"<id animal>"}`. Retire ensuite
   l’animal avec `DELETE /lots/{lotId}/animaux/{animalId}`. Les deux opérations
   renvoient `204` et peuvent être répétées.

```json
{
  "identifiant": "TRUIE-DEMO",
  "especeCode": "PORC",
  "sexe": "FEMELLE",
  "origine": "NE_SUR_PLACE"
}
```

Pour peser l’animal, envoie `POST /evenements` avec le corps suivant, en
remplaçant les identifiants et la date par ceux de ton jeu de test.

```json
{
  "type": "PESEE",
  "exploitationId": "<id exploitation>",
  "animalId": "<id animal>",
  "dateEvenement": "2026-01-10",
  "methode": "BALANCE",
  "poidsKg": 34.5
}
```

La réponse `201` contient le poids enregistré. Pour une pesée de lot, remplace
`animalId` par `lotId` et indique `nbEchantillon`. Pour une estimation au ruban
sur un bovin, sélectionne `RUBAN` et renseigne `perimetreThoraciqueCm` et
`longueurCm`. Le serveur calcule le poids.

Consulte les écritures avec `GET /animaux/{id}/historique` ou
`GET /evenements?lotId=<id lot>&type=PESEE`. Sur `/evenements`, renseigne exactement
une cible. Les paramètres `du` et `au` acceptent les dates ISO.

## Tester l’inscription par e-mail et les sessions

Dans ton `.env` local, ajoute `EMAIL_MODE=console` et redémarre Fitombo en profil
`dev`. Le code de test s’affiche dans les logs de l’application. Utilise une
adresse qui ne correspond à aucun compte de la base de test.

1. Envoie `POST /auth/inscription` avec `{"email":"tiana@example.test"}` et
   récupère le `demandeId` renvoyé par la réponse `202`.
2. Envoie `POST /auth/verification-email` avec ce corps et le code des logs.
   L’inscription réussie renvoie `201`.

```json
{
  "demandeId": "<identifiant de la demande>",
  "email": "tiana@example.test",
  "code": "<code à six chiffres>",
  "nom": "Tiana",
  "langue": "mg",
  "motDePasse": "Inscription-demo-2026!"
}
```

3. Connecte-toi avec `POST /auth/connexion` en envoyant les champs `email` et `motDePasse`. Récupère `accessToken` et
   `refreshToken`. Lance `GET /me` avec l’access token comme Bearer pour
   vérifier le compte.
4. Envoie `POST /auth/refresh` avec `{"refreshToken":"<jeton courant>"}`.
   Remplace les deux jetons par ceux de la réponse `200`.
5. Envoie `POST /auth/deconnexion` avec le nouveau jeton d’accès dans
   **Authorization → Bearer Token** et le nouveau refresh token dans le
   corps JSON. Le serveur renvoie `204`. Une tentative de renouvellement de
   cette session doit ensuite renvoyer `401`.

Dans Postman, sélectionne **No Auth** sur les quatre appels publics
(inscription, vérification e-mail, connexion et refresh). Le code expire après
cinq minutes, le renvoi est limité à un par minute et cinq erreurs bloquent la
demande. Pour vérifier le rejeu, réutilise un ancien refresh token après une
rotation. L’ancien et son successeur doivent alors être refusés.

Pour l’envoi réel, vérifie une adresse expéditrice dans Brevo, crée une clé
d’API et configure `EMAIL_MODE=brevo`, `EMAIL_FROM` et `BREVO_API_KEY` dans
l’environnement. Render Free peut utiliser ce transport HTTPS.

## Enregistrer les soins, l’alimentation et la production

Dans Postman, envoie `POST {{baseUrl}}/evenements` avec le jeton du propriétaire
de l’exploitation. Utilise `Content-Type: application/json` et le corps suivant
en remplaçant les identifiants par ceux de tes données locales.

```json
{
  "type": "ALIMENTATION",
  "exploitationId": "{{exploitationId}}",
  "animalId": "{{animalId}}",
  "dateEvenement": "2026-06-01",
  "aliment": "Foin",
  "quantiteKg": 12.5,
  "coutAriary": 15000
}
```

Pour un lot, remplace `animalId` par `lotId`. Utilise une date passée ou la
date du jour. Pour les autres types, remplace les champs propres à
l’alimentation par ceux de la ligne correspondante.

| Type | Champs d’exemple |
| --- | --- |
| `VACCINATION` | `"vaccin":"Vaccin enregistré","lotVaccin":"DOSE-A","rappelPrevu":"2026-12-01"` |
| `TRAITEMENT` | `"produit":"Produit enregistré","dose":"4 ml","dureeJours":3,"delaiAttenteJours":7` |
| `PRODUCTION` | `"typeProduction":"LAIT","quantite":7.5,"unite":"LITRE"` |

Vérifie le statut `201 Created` et copie l’identifiant `id` retourné.
Consulte ensuite `GET {{baseUrl}}/evenements?animalId={{animalId}}` pour
retrouver la saisie dans le journal. Pour un lot, utilise le paramètre `lotId`.

Pour annuler la saisie, envoie
`PATCH {{baseUrl}}/evenements/{{evenementId}}/annulation` avec
`{"motif":"Saisie en double"}`. Vérifie le statut `204 No Content` puis
recharge le journal. La saisie y apparaît avec le statut `ANNULE`.

Pour corriger la saisie, utilise un événement encore valide. Envoie
`PUT {{baseUrl}}/evenements/{{evenementId}}` avec un objet JSON contenant
`motif` et `saisie`. Dans `saisie`, reprends le corps du `POST` avec le
même type, la même exploitation et la même cible, puis modifie les valeurs
à corriger. Le statut `201 Created` renvoie l’identifiant de la nouvelle
saisie. Dans le journal, l’original passe à `ANNULE` et la nouvelle ligne
référence l’original dans `corrigeEvenementId`.

## Enregistrer un soin sur plusieurs animaux

Envoie `POST {{baseUrl}}/evenements/groupe` avec le jeton du propriétaire et
le corps suivant. Remplace les deux identifiants par ceux d’animaux actifs
de la même exploitation.

```json
{
  "animalIds": ["{{animalId}}", "{{autreAnimalId}}"],
  "saisie": {
    "type": "VACCINATION",
    "exploitationId": "{{exploitationId}}",
    "dateEvenement": "2026-06-01",
    "vaccin": "Vaccination groupée"
  }
}
```

La réponse `201 Created` contient un événement par animal. Pour un traitement,
utilise le type `TRAITEMENT` et les champs du traitement. Pour vérifier le
contrôle de la liste, envoie deux fois le même identifiant. La réponse doit
être `400 Bad Request` et les journaux doivent garder le même nombre de saisies.

## Suivre la reproduction

Dans Postman, envoie ces saisies avec `POST {{baseUrl}}/evenements` et le
jeton du propriétaire. Reprends les champs communs du journal et choisis une
femelle active dont l’espèce a une durée de gestation renseignée.

| Type | Champs à ajouter |
| --- | --- |
| `SAILLIE` | `"methode":"MONTE_NATURELLE","maleId":"{{maleId}}"`. Pour une insémination, utilise `INSEMINATION_ARTIFICIELLE` et, si connue, `referenceSemence`. |
| `DIAGNOSTIC_GESTATION` | `"positif":true,"methode":"ECHOGRAPHIE","saillieId":"{{saillieId}}"` |
| `MISE_BAS` | `"nesVivants":1,"mortsNes":0,"saillieId":"{{saillieId}}","petits":[{"identifiant":"PETIT-01","sexe":"FEMELLE"}]` |

Récupère `saillieId` dans la réponse à la première requête. Consulte
`GET /evenements/{{saillieId}}` pour voir `details.miseBasPrevue`. Choisis des dates
chronologiques, au plus tard aujourd’hui. Après la mise bas, vérifie dans
`GET /exploitations/{{exploitationId}}/animaux` que le petit a été créé.
Pour une cible lot, indique `codeLotPetits` à la place de `petits` et vérifie
le nouveau lot dans `GET /exploitations/{{exploitationId}}/lots`.

## Enregistrer les mouvements et les pertes

Envoie aussi ces saisies via `POST {{baseUrl}}/evenements`.

| Cas | Champs à ajouter |
| --- | --- |
| Vente d’un animal | `"type":"MOUVEMENT","typeMouvement":"VENTE","quantite":1,"nomTiers":"Acheteur","prixTotalAriary":300000,"poidsTotalKg":120,"numeroPieceCirculation":"PC-01"` |
| Transfert | `"type":"MOUVEMENT","typeMouvement":"TRANSFERT","quantite":1,"exploitationDestinationId":"{{destinationId}}"` |
| Confiage puis retour | `"type":"MOUVEMENT","typeMouvement":"CONFIAGE","quantite":1,"gardienId":"{{eleveurGardienId}}","conditions":"Garde temporaire"`, puis `RETOUR_CONFIAGE` avec la quantité et sans les champs du gardien. |
| Décès ou perte d’un lot | `"type":"DECES","cause":"MALADIE","nombre":2`, avec `lotId` comme cible. Pour un vol, choisis `VOL`. |

`ACHAT` consigne l’achat d’une cible déjà inscrite. `ABATTAGE` suit le même
format que la vente. Pour un transfert entre exploitations, utilise deux
exploitations que tu gères. Un transfert de lot déplace le lot entier et exige
qu’aucun animal individuel n’y soit rattaché pour un changement d’exploitation.
Pour un déplacement interne, indique `emplacementDestinationId`. Consulte
`GET /evenements/{{mouvementId}}` pour vérifier l’origine et la destination.

Une vente ou un abattage effectué pendant un délai d’attente renvoie
`delaiAttenteJusquAu` dans la réponse. Vérifie ensuite l’état de l’animal
ou le nouvel effectif du lot. Une quantité supérieure à l’effectif doit être
refusée. La révision générique renvoie `409` pour une saillie liée à un diagnostic ou à une mise bas valide, une mise bas,
un mouvement ou un décès, car ces saisies ont des effets ou des dépendances
qui demandent une procédure de révision dédiée.

## Consulter la croissance et gérer les objectifs

Fais deux pesées valides à des dates distinctes puis appelle
`GET /animaux/{{animalId}}/croissance` ou `GET /lots/{{lotId}}/croissance`.
Vérifie les points de pesée et le gain moyen quotidien. Lorsque la race et
l’âge permettent la comparaison, la réponse fournit les bornes de référence
et une position. Pour un lot dont l’effectif est stable, enregistre aussi
l’alimentation entre les deux pesées pour obtenir l’indice de consommation.

Envoie `POST /objectifs?exploitationId={{exploitationId}}` avec le jeton
du propriétaire et ce corps en adaptant les dates et le poids.

```json
{
  "animalId": "{{animalId}}",
  "type": "POIDS_CIBLE",
  "unite": "KG",
  "valeurCible": 120,
  "dateDebut": "2026-06-01",
  "dateCible": "2027-01-01"
}
```

| Type | Unités |
| --- | --- |
| `POIDS_CIBLE` | `KG`, avec une cible animal ou lot. |
| `EFFECTIF_CIBLE` | `TETES`, avec un nombre entier. |
| `PRODUCTION_CIBLE` | `LITRE_PAR_JOUR` ou `OEUFS_PAR_JOUR`, calculé sur la dernière journée renseignée. |
| `MARGE_CIBLE` | `ARIARY` ou `POURCENT`, à partir des recettes et des dépenses enregistrées depuis le début de l’objectif. |

Appelle `GET /objectifs?exploitationId={{exploitationId}}` pour la liste ou
`GET /objectifs/{{objectifId}}/progression` pour suivre un objectif. Une nouvelle
pesée peut faire changer le pourcentage, le gain restant et la date estimée.
L’estimation de date utilise les deux dernières pesées valides de jours
différents. Pour réviser l’objectif, envoie `PUT /objectifs/{{objectifId}}`
avec `valeurCible`, `dateCible` et éventuellement `notes`. Pour l’abandonner,
envoie `DELETE /objectifs/{{objectifId}}`.

## Consulter un lot et son journal

Avec un compte qui a accès à l’exploitation, appelle `GET /lots/{{lotId}}`
pour obtenir la fiche du lot et `GET /lots/{{lotId}}/historique` pour consulter
son journal. La route historique accepte la pagination habituelle.

## Saisir une transaction et consulter la marge

Avec le jeton du propriétaire, envoie
`POST /exploitations/{{exploitationId}}/transactions`.

```json
{
  "categorie": "ALIMENT",
  "montantAriary": 40000,
  "dateTransaction": "2026-06-01",
  "libelle": "Achat de foin"
}
```

La catégorie détermine le sens de la transaction. Pour enregistrer une recette,
utilise par exemple `VENTE_LAIT`. Renseigne `animalId` ou `lotId` pour affecter
la transaction. Sans ces champs, elle concerne l’exploitation entière.

Liste les écritures avec `GET /exploitations/{{exploitationId}}/transactions`.
Consulte `GET /exploitations/{{exploitationId}}/marges?du=2026-06-01&au=2026-06-30`
pour le total de la période. Ajoute `animalId` ou `lotId` pour restreindre le calcul.
Le coût par kilogramme utilise les productions valides saisies en kilogrammes.

## Inviter un membre et révoquer son accès

Avec le jeton du propriétaire, envoie `POST /exploitations/{{exploitationId}}/membres`
avec `{"utilisateurId":"{{compteInviteId}}","role":"VETERINAIRE"}`. Le destinataire
doit avoir un compte actif. Récupère l’identifiant de l’invitation dans la réponse.

Avec le jeton du destinataire, liste les invitations dans `GET /me/invitations`
puis accepte avec `POST /me/invitations/{{invitationId}}/acceptation`. Le compte
peut alors consulter les ressources ouvertes aux membres de l’exploitation.

Avec le jeton du propriétaire, appelle
`DELETE /exploitations/{{exploitationId}}/membres/{{invitationId}}`. Vérifie que
le compte invité perd immédiatement l’accès.

## Modifier ou archiver une exploitation ou un emplacement

Avec le jeton du propriétaire, utilise `PUT /exploitations/{{exploitationId}}`
ou `PUT /emplacements/{{emplacementId}}` avec les mêmes champs que pour la
création. Vérifie ensuite la modification dans la liste correspondante.

Appelle la même URL avec `DELETE` pour archiver l’élément. La réponse est
`204 No Content`. L’élément disparaît des lectures courantes et les données
historiques restent en base.

## Consulter le tableau de bord

Avec le jeton du propriétaire, envoie
`GET /exploitations/{{exploitationId}}/tableau-de-bord?du=2026-06-01&au=2026-06-30`.
L’effectif, les objectifs en cours et les alertes du jour sont calculés à la
date de consultation. Les décès et la marge couvrent la période demandée.

## Consulter les protocoles et les rappels

Appelle `GET /especes/{{especeCode}}/protocoles` avec ton jeton pour consulter
les étapes du référentiel et leur indicateur `valide`. La tâche quotidienne
crée des alertes pour les étapes des protocoles actifs et validés, en partant
de la date de naissance de l’animal ou de la date de création du lot.
Les protocoles de démonstration restent indicatifs jusqu’à leur validation.

Consulte `GET /alertes` avec le jeton du propriétaire. Le traitement quotidien
couvre les échéances du jour et des sept jours suivants, sans recréer
une alerte déjà enregistrée pour la même échéance. Le paramètre Spring
`app.alertes.cron` permet d’adapter l’heure de déclenchement.

Une pesée en retard crée aussi un rappel si la fréquence de pesée de
l’espèce est renseignée. L’échéance repart de la dernière pesée valide ou,
pour une première mesure, de la date de référence de l’animal ou du lot.

Un objectif échu qui reste sous sa cible crée une alerte
`OBJECTIF_EN_RETARD` pour le propriétaire. Une même échéance ne génère
qu’une alerte. Le calcul reprend les mesures de la page de progression.

## Télécharger un registre PDF ou un classeur Excel

Dans Postman, crée une requête **GET** avec le jeton Bearer et choisis
**Send and Download** pour enregistrer le fichier.

| Fichier | Route | Accès |
| --- | --- | --- |
| Registre d’un animal | `/animaux/{{animalId}}/registre.pdf` | Comptes ayant accès à l’animal |
| Registre d’un lot | `/lots/{{lotId}}/registre.pdf` | Comptes ayant accès au lot |
| Événements et finances | `/exploitations/{{exploitationId}}/export.xlsx` | Propriétaire |

Ajoute si besoin `?du=2026-06-01&au=2026-06-30`. Les deux bornes sont
facultatives et inclusives. Le registre indique le statut des événements,
y compris les annulations. Le classeur contient les feuilles **Événements**
et **Finances**. Les dates et les montants sont triables dans Excel.
Si une liste dépasse 10 000 lignes, l’API renvoie `422`. Réduis la période
et télécharge les données en plusieurs fichiers.

## Donner ou révoquer le partage avec une organisation

Avec le jeton de l’éleveur, consulte `GET /me/partage-organisation`. Si une
organisation est rattachée au profil, envoie à `PUT /me/partage-organisation`
le corps `{"partage":true,"organisationId":"{{organisationId}}"}`.
Pour révoquer le partage, envoie `{"partage":false}`.

Avec le jeton du représentant de l’organisation, consulte
`GET /organisation/statistiques`. Par défaut, l’API fournit des totaux à
partir de trois éleveurs consentants. Elle renvoie `404` en dessous de ce
seuil ou si le compte est hors périmètre.

## Synchroniser le journal d’événements

Pour envoyer des saisies conservées hors ligne, appelle `POST /sync/push`
avec le jeton du propriétaire et au plus 50 opérations. Chaque opération a
un UUID créé et conservé par le client dans `operationId` et le corps
habituel de `POST /evenements` dans `saisie`.

```json
{
  "operations": [{
    "operationId": "65f24876-cce1-4b52-81f0-818280788a1e",
    "saisie": {
      "type": "PESEE",
      "exploitationId": "{{exploitationId}}",
      "animalId": "{{animalId}}",
      "dateEvenement": "2026-06-01",
      "methode": "BALANCE",
      "poidsKg": 120
    }
  }]
}
```

Renvoie le même corps pour vérifier que la première réponse `ACCEPTE` est
suivie de `DOUBLON` avec le même `evenementId`. Un identifiant déjà accepté
avec un corps différent renvoie `REJETE`.

Pour lire le journal synchronisé, appelle
`GET /sync/pull?exploitationId={{exploitationId}}` avec un compte autorisé.
Conserve le `curseur` par exploitation après avoir appliqué la page localement.
Au prochain appel, renvoie ses valeurs dans les paramètres `transaction`
et `sequence`. Continue tant que `suite` vaut `true`. Applique chaque
résultat par identifiant d’événement et retire l’entrée locale si `retire`
vaut `true`. L’état renvoyé résume l’entrée du journal. La file d’attente
et la base locale du téléphone restent à intégrer au client.
