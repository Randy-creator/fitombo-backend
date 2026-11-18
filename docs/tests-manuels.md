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
