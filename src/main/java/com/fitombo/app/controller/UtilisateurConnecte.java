package com.fitombo.app.controller;

import java.util.UUID;

import com.fitombo.app.exception.CodeErreur;
import com.fitombo.app.exception.ExceptionApplicative;

import org.springframework.security.oauth2.jwt.Jwt;

final class UtilisateurConnecte {

    private UtilisateurConnecte() {
    }

    static UUID id(Jwt jwt) {
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
