package com.fitombo.app.controller;

import com.fitombo.app.exception.CodeErreur;
import com.fitombo.app.exception.ExceptionApplicative;
import java.util.UUID;
import org.springframework.security.oauth2.jwt.Jwt;

public final class UtilisateurConnecte {

  private UtilisateurConnecte() {}

  public static UUID id(Jwt jwt) {
    if (jwt == null || jwt.getSubject() == null) {
      throw new ExceptionApplicative(CodeErreur.UTILISATEUR_NON_IDENTIFIE);
    }
    try {
      return UUID.fromString(jwt.getSubject());
    } catch (IllegalArgumentException exception) {
      throw new ExceptionApplicative(CodeErreur.UTILISATEUR_NON_IDENTIFIE);
    }
  }
}
