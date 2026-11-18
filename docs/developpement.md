# Développer sur Fitombo

Commencer par le [cahier des charges](Cahier%20des%20charges%20%E2%80%94%20Fitombo.pdf)
pour les règles métier et l'[UML](elevage-uml.mermaid) pour les relations.
Pour le mapping JPA, vérifier aussi les migrations dans
[`src/main/resources/db/migration`](../src/main/resources/db/migration).
Si ces sources divergent, signaler l’écart avant de changer le comportement.

## Structure Java

Sous `com.fitombo.app`, organiser le code d’abord par couche, puis par domaine si nécessaire.

| Package | Responsabilité |
| --- | --- |
| `config` | Configuration Spring, dont la sécurité dans `config.security`. |
| `controller` | HTTP, validation des entrées, DTO et réponses. |
| `service` | Cas d’usage, transactions, règles métier et autorisations par exploitation. |
| `repository` | Requêtes et persistance Spring Data JPA. |
| `dto` | Contrats de réponse et de requête. |
| `model` | Entités et enums regroupés par domaine, par exemple `model.cheptel` et `model.evenement`. |

Suivre le flux `controller → service → repository`. Séparer les DTO des entités JPA.
Utiliser l’injection par constructeur, des noms explicites et des commentaires courts
pour expliquer les décisions qui ne se lisent pas dans le code.
Centraliser les erreurs HTTP avec `@RestControllerAdvice`, des réponses Problem Details
et des codes métier stables. Les services expriment leurs erreurs sans statut HTTP. Actualiser OpenAPI quand le contrat HTTP évolue.

## Lisibilité et configuration

- Garder les contrôleurs centrés sur les entrées HTTP, l’appel au service et la réponse.
- Donner une responsabilité claire à chaque méthode. Nommer les étapes intermédiaires quand cela rend le cas d’usage plus lisible. Extraire les blocs qui ont une responsabilité propre, comme émettre un JWT, vérifier un compte ou limiter les tentatives.
- Placer les réglages modifiables dans les propriétés Spring. Utiliser `@ConfigurationProperties` pour les groupes de paramètres et valider leurs valeurs au démarrage. Garder en Java les constantes qui décrivent un invariant technique.
- Centraliser la conversion des exceptions applicatives en statuts et codes HTTP. Pour la validation du login, renvoyer une erreur générique sans reprendre les valeurs soumises ni le détail du parseur.
- Garder les requêtes et projections dans les repositories. Définir le périmètre et les transactions dans les services. Tester le parcours par son point d’entrée normal.

## Règles métier

- **RG-01** — Un animal dépend d’une seule exploitation à la fois. Un transfert crée un événement.
- **RG-02** — Suivre les espèces par individu ou par lot selon leur mode de suivi configuré.
- **RG-03** — Chaque événement cible exactement un animal ou un lot.
- **RG-04** — Un animal vendu, abattu, mort, perdu ou volé ne reçoit que des événements de correction.
- **RG-05** — Décès, vente et abattage partiel réduisent l’effectif du lot. Garder l’effectif à zéro ou plus.
- **RG-06** — Calculer la progression depuis les données métier. Pour un objectif de poids, utiliser la dernière pesée valide.
- **RG-07** — Le gain moyen quotidien est la différence entre deux poids divisée par le nombre de jours.
- **RG-08** — L’indice de consommation d’un lot est la quantité d’aliment consommée divisée par le gain de poids total.
- **RG-09** — Pour un bovin, le poids estimé en kg vaut environ `périmètre² × longueur × 80`, avec les longueurs en mètres. Afficher la méthode et la précision.
- **RG-10** — Une vente ou un abattage pendant le délai d’attente d’un traitement déclenche un avertissement.
- **RG-11** — Calculer la date de mise bas prévue en ajoutant la durée de gestation de l’espèce à la date de saillie.
- **RG-12** — Le gardien et le propriétaire peuvent être distincts. L’accès aux finances est réservé au propriétaire.
- **RG-13** — L’éleveur garde la maîtrise de ses données. Le partage avec une organisation ou un vétérinaire exige son accord et doit pouvoir être révoqué.
- **RG-14** — Les champs doivent s’adapter aux pratiques locales et aux différences régionales, notamment aux fady.

Conserver l’historique des événements. Pour corriger une saisie, annuler l’original
et le relier à l’événement correctif. Prévoir les doublons, l’ordre inversé et les
conflits dans les traitements de synchronisation hors ligne.

## PostgreSQL local

La connexion et le pool Hikari sont configurés dans
[`config/persistence.properties`](../src/main/resources/config/persistence.properties).
Spring Boot crée le `DataSource`. Les repositories accèdent à la base via JPA.
Pour un repository Spring Data, déclarer une interface typée avec l’entité et
son type d’identifiant.

Créer le `.env` depuis [`.env.example`](../.env.example) si nécessaire, puis renseigner
`DB_URL`, `DB_USERNAME`, `DB_PASSWORD` et une clé `JWT_SECRET` aléatoire.
Docker Compose utilise aussi `DB_NAME` et `DB_PORT`. Garder les secrets hors de Git.

```bash
docker compose up -d postgres
```

Dans DBeaver Community, choisir PostgreSQL et utiliser `127.0.0.1` sur le port
`5433` avec la base et les identifiants du `.env`. Actualiser l’onglet **Données**
après une écriture ou le dossier **Tables** après une migration.
Le port JDBC de l’application exécutée sur la machine doit correspondre
au port publié par Compose.

Flyway applique les migrations et Hibernate valide le schéma au démarrage.
Ajouter une nouvelle migration pour chaque évolution d’un schéma déjà migré.
Flyway vérifie aussi le checksum des fichiers dont seuls les commentaires ont changé.

## Sécurité et vérification

- Les routes privées exigent un JWT d’accès Bearer. Les rôles globaux viennent du JWT. Vérifier l’appartenance à l’exploitation dans le service.
- Le login par mot de passe exige un compte actif, un téléphone vérifié au format E.164 et un hash BCrypt. Renvoyer la même erreur pour les identifiants invalides et les comptes non admissibles. Protéger la réponse JWT contre la mise en cache.
- Le login limite les tentatives par adresse côté application. Les réglages `app.security.login.*` définissent le nombre de tentatives, la capacité et la durée de la fenêtre. En déploiement derrière un proxy, transmettre l’adresse cliente depuis un proxy de confiance. Mutualiser la limitation côté proxy ou via un stockage partagé si plusieurs instances du backend servent les requêtes.
- Pour les futurs parcours d’authentification, prévoir les codes SMS à usage unique ainsi que la rotation et la révocation des jetons de rafraîchissement.
- Tester les calculs et règles métier à l’unité, les migrations et repositories avec PostgreSQL, et les cas d’usage HTTP avec MockMvc ou RestAssured. Couvrir les conflits et doublons pour la synchronisation.
- À la fin de chaque fonctionnalité ou changement notable, lancer les vérifications pertinentes puis créer un commit dédié avec un message court. Suivre le format de l’historique, par exemple `feat(cheptel): add animal model` ou `fix(configuration): align local PostgreSQL port`.

## Lectures paginées

- Projeter les colonnes utiles vers un DTO dans le repository pour éviter les chargements paresseux en série.
- Appliquer le périmètre utilisateur et les filtres en base. Exclure les lignes supprimées logiquement des lectures courantes.
- Renvoyer `items`, `page`, `size`, `totalElements` et `totalPages`. La pagination commence à zéro. Limiter le tri aux champs exposés et départager les égalités par un identifiant.
- Utiliser le `sub` du JWT comme UUID de l’utilisateur. Une exploitation est accessible à son propriétaire actif ou à un membre actif dont les dates sont valides. Pour une ressource hors périmètre, renvoyer `404`.
- Lancer `./mvnw test` avec Docker pour les tests d’intégration PostgreSQL. Voir le [guide de tests manuels](tests-manuels.md) pour vérifier les API localement.
