package com.fitombo.app.exception;

public enum CodeErreur {
  IDENTIFIANTS_INVALIDES("Identifiants invalides"),
  LIMITE_CONNEXION_ATTEINTE("Trop de tentatives de connexion"),
  UTILISATEUR_NON_IDENTIFIE("Identifiant utilisateur absent ou invalide"),
  TRI_INVALIDE("Champ de tri non autorisé"),
  ESPECE_INTROUVABLE("Espèce introuvable"),
  EXPLOITATION_INTROUVABLE("Exploitation introuvable"),
  PROFIL_INTROUVABLE("Profil introuvable");

  private final String message;

  CodeErreur(String message) {
    this.message = message;
  }

  public String message() {
    return message;
  }
}
