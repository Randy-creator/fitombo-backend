package com.fitombo.app.exception;

public enum CodeErreur {
  IDENTIFIANTS_INVALIDES("Identifiants invalides"),
  LIMITE_CONNEXION_ATTEINTE("Trop de tentatives de connexion"),
  UTILISATEUR_NON_IDENTIFIE("Identifiant utilisateur absent ou invalide"),
  TRI_INVALIDE("Champ de tri non autorisé"),
  ESPECE_INTROUVABLE("Espèce introuvable"),
  EXPLOITATION_INTROUVABLE("Exploitation introuvable"),
  PROFIL_INTROUVABLE("Profil introuvable"),
  ANIMAL_INTROUVABLE("Animal introuvable"),
  ALERTE_INTROUVABLE("Alerte introuvable"),
  ELEVEUR_INTROUVABLE("Profil éleveur introuvable"),
  EXPLOITATION_NOM_UTILISE("Ce nom d’exploitation est déjà utilisé"),
  EMPLACEMENT_NOM_UTILISE("Ce nom d’emplacement est déjà utilisé"),
  LOT_CODE_UTILISE("Ce code de lot est déjà utilisé"),
  ESPECE_NON_SUIVIE_EN_LOT("Cette espèce est suivie individuellement"),
  RACE_INCOMPATIBLE("Race indisponible pour cette espèce"),
  EMPLACEMENT_INTROUVABLE("Emplacement introuvable dans cette exploitation"),
  ANIMAL_IDENTIFIANT_UTILISE("Cet identifiant est déjà utilisé"),
  ANIMAL_BOUCLE_UTILISEE("Cette boucle est déjà utilisée"),
  ESPECE_SUIVIE_EN_LOT("Cette espèce est suivie en lot"),
  PARENT_INCOMPATIBLE("Parent indisponible pour cette espèce et ce sexe"),
  LOT_INTROUVABLE("Lot introuvable"),
  LOT_CLOTURE("Le lot doit être actif"),
  ANIMAL_NON_ACTIF("L’animal doit être actif"),
  ANIMAL_DEJA_RATTACHE("Cet animal est déjà rattaché à un autre lot"),
  ESPECE_LOT_INCOMPATIBLE("L’animal et le lot doivent être de la même espèce"),
  PESEE_INVALIDE("Mesures ou cible de la pesée invalides"),
  CODE_EMAIL_INVALIDE("Code e-mail invalide ou expiré"),
  EMAIL_INDISPONIBLE("Envoi e-mail indisponible"),
  LIMITE_EMAIL_ATTEINTE("Réessaie avant de demander un autre code"),
  COMPTE_DEJA_INSCRIT("Un compte existe déjà pour ces coordonnées"),
  SESSION_INVALIDE("Session invalide ou expirée");

  private final String message;

  CodeErreur(String message) {
    this.message = message;
  }

  public String message() {
    return message;
  }
}
