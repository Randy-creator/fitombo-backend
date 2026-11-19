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
