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

La réponse `200` fournit `accessToken`, `tokenType` et `expiresIn` (1 800 secondes
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
