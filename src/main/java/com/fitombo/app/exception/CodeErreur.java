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
  PARENT_INCOMPATIBLE("Parent indisponible pour cette espèce et ce sexe");

  private final String message;

  CodeErreur(String message) {
    this.message = message;
  }

  public String message() {
    return message;
  }
}
