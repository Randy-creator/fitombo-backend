# Fitombo

Lire [le guide de développement](docs/developpement.md) avant de modifier le code.
Il détaille les règles métier, les conventions du projet et les procédures locales.

- Organiser le Java par couche, puis par fonctionnalité. Utiliser le même nom de sous-package dans les couches concernées.
- Garder les contrôleurs centrés sur HTTP et les DTO. Placer les règles métier, les autorisations et les transactions dans les services, les requêtes dans les repositories et la conversion des erreurs HTTP dans les conseils Spring.
- Regrouper les routes d’une même ressource ou d’un même parcours dans un contrôleur. Garder des services distincts pour les responsabilités métier différentes.
- Utiliser `@UtilisateurCourant UUID utilisateurId` pour récupérer l’identité dans un contrôleur. Réutiliser le contrôle d’accès existant pour toute ressource rattachée à une exploitation.
- Chercher une règle, une requête, une projection ou une fixture équivalente avant d’en ajouter une. Mutualiser les éléments qui doivent évoluer ensemble.
- Employer des noms explicites, des méthodes à responsabilité claire et des commentaires courts. Placer les réglages modifiables dans la configuration Spring et garder les secrets hors de Git.
- Ajouter une migration Flyway pour chaque évolution du schéma existant. Traiter les migrations déjà appliquées comme immuables, commentaires compris.
- Couvrir les nouveaux comportements et les refus d’accès par des tests. Appliquer `./mvnw spotless:apply`, puis lancer `./mvnw verify` avec Java 25 et Docker.
- Créer un commit dédié et court après chaque fonctionnalité ou changement notable vérifié.
