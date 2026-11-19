# Développer sur Fitombo

Lire ce guide avant toute modification de code. Consulter ensuite le [cahier des charges](Cahier%20des%20charges%20%E2%80%94%20Fitombo.pdf)
pour les règles métier et l'[UML](elevage-uml.mermaid) pour les relations.
Pour le mapping JPA, vérifier aussi les migrations dans
[`src/main/resources/db/migration`](../src/main/resources/db/migration).
Si ces sources divergent, signaler l’écart avant de changer le comportement.

## Structure Java

Sous `com.fitombo.app`, organiser le code d’abord par couche, puis par fonctionnalité ou domaine.
Réutiliser le même nom de sous-package dans les couches concernées. Par exemple,
le login regroupe ses classes dans `controller.connexion`, `dto.connexion`,
`service.connexion`, `repository.connexion` et `config.security.connexion`.
Regrouper aussi les tests dédiés sous `src/test/java/com/fitombo/app/connexion`.

Placer chaque classe dans le sous-package qui correspond à sa responsabilité.
Les composants partagés restent dans leur package commun, comme la chaîne
Spring Security dans `config.security`, la gestion globale des erreurs dans
`controller` et l’entité `Utilisateur` dans `model.utilisateur`.
Garder une visibilité de package pour les classes et méthodes internes au sous-package.

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

## Réutilisation et optimisation

- Avant d’ajouter du code, chercher une opération, une validation ou une requête équivalente dans sa couche. Réutiliser le même contrat lorsque les entrées, les autorisations et les erreurs suivent la même règle.
- Centraliser les règles qui doivent évoluer ensemble. Une ressemblance de structure entre deux cas d’usage ne suffit pas à justifier une abstraction. Garder les routes, DTO et tris propres à chaque domaine.
- Déclarer `@UtilisateurCourant UUID utilisateurId` dans les contrôleurs qui ont besoin du compte connecté. Le résolveur MVC lit le `sub` du JWT. Les services utilisent l’UUID et appliquent les autorisations métier.
- Réutiliser `ExploitationService.verifierAcces` pour les parcours qui vérifient seulement le périmètre d’une exploitation. Utiliser la projection de détail quand la réponse a besoin des données de l’exploitation.
- Projeter uniquement les champs nécessaires, paginer en base et réutiliser les projections identiques. Vérifier les requêtes et les volumes réels avant d’introduire un cache ou un index.
- Réutiliser les propriétés communes et les fixtures d’intégration. Les tests de bout en bout doivent couvrir le comportement partagé, notamment les cas hors périmètre.

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

## Créations et concurrence

- Déterminer le propriétaire à partir de l’utilisateur authentifié. Pour les écritures réservées au propriétaire, réutiliser `ExploitationService.chargerPourGestion`.
- Vérifier en service les références optionnelles, notamment l’espèce de la race et l’appartenance de l’emplacement à l’exploitation.
- S’appuyer sur les contraintes d’unicité PostgreSQL pour arbitrer les écritures concurrentes. Traduire uniquement la contrainte attendue en conflit métier avec `ConflitsPersistance`.
- Enregistrer les coordonnées d’une exploitation par paire et les garder privées lors de la création.

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
- Réutiliser le périmètre d’exploitation pour les consultations par identifiant et pour les alertes rattachées à une exploitation. Restreindre aussi chaque alerte à son destinataire.
- Pour une transition répétable comme marquer une alerte vue, préserver les états terminaux et protéger la lecture suivie d’une mise à jour dans la même transaction.
- Lancer `./mvnw test` avec Docker pour les tests d’intégration PostgreSQL. Voir le [guide de tests manuels](tests-manuels.md) pour vérifier les API localement.

## Formater et vérifier avant de pousser

Utiliser le JDK 25 et démarrer Docker pour les tests PostgreSQL. Le wrapper Maven
télécharge Spotless et Google Java Format au premier lancement.

```bash
./mvnw spotless:apply
./mvnw --batch-mode --no-transfer-progress verify
```

Spotless applique le style Google au Java de production et de test. Sur les
fichiers de configuration ciblés, il vérifie les espaces en fin de ligne et le
saut de ligne final. Les migrations SQL sont exclues pour préserver les checksums
Flyway. Pour une vérification du formatage seul, utiliser `./mvnw spotless:check`.

`verify` contrôle le formatage, compile, lance les tests et construit le JAR.
Les tests d’intégration provisionnent leur propre PostgreSQL via Testcontainers.
Le workflow [CI](../.github/workflows/ci.yml) exécute la même commande à chaque
push et pull request. Le contrôle est en lecture seule. En cas d’écart de
formatage, Spotless indique les fichiers à corriger et le job s’arrête avant
la compilation et les tests. Appliquer les corrections en local avec
`./mvnw spotless:apply`, relire le diff, puis committer et pousser.

### Activer et suivre la CI sur GitHub

1. Pousser la branche qui contient le workflow, par exemple avec `git push -u origin dev`.
2. Dans le dépôt GitHub, ouvrir **Actions → CI** et vérifier le résultat du job
   **Maven verify**. Si les workflows sont désactivés, les autoriser dans
   **Settings → Actions → General**. Aucun secret de déploiement ne doit être ajouté.
3. Pour rendre le contrôle obligatoire avant fusion, après une première
   exécution, ouvrir **Settings → Rules → Rulesets**, créer une règle pour la
   branche cible, exiger une pull request et sélectionner le contrôle **Maven verify**
   dans **Require status checks to pass**.
