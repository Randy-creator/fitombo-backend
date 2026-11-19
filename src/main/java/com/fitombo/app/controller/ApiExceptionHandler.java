package com.fitombo.app.controller;

import com.fitombo.app.exception.ExceptionApplicative;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class ApiExceptionHandler {

  @ExceptionHandler(ExceptionApplicative.class)
  ProblemDetail traiter(ExceptionApplicative exception) {
    HttpStatus statut =
        switch (exception.code()) {
          case OBJECTIF_INVALIDE -> HttpStatus.BAD_REQUEST;
          case OBJECTIF_INTROUVABLE -> HttpStatus.NOT_FOUND;
          case MOUVEMENT_INVALIDE -> HttpStatus.BAD_REQUEST;
          case REPRODUCTION_INVALIDE -> HttpStatus.BAD_REQUEST;
          case REVISION_AVEC_EFFETS -> HttpStatus.CONFLICT;
          case SORTIE_CHEPTEL_INVALIDE -> HttpStatus.BAD_REQUEST;
          case ELEVEUR_INTROUVABLE -> HttpStatus.NOT_FOUND;
          case EXPLOITATION_NOM_UTILISE -> HttpStatus.CONFLICT;
          case EMPLACEMENT_NOM_UTILISE -> HttpStatus.CONFLICT;
          case LOT_CODE_UTILISE -> HttpStatus.CONFLICT;
          case ESPECE_NON_SUIVIE_EN_LOT -> HttpStatus.BAD_REQUEST;
          case RACE_INCOMPATIBLE -> HttpStatus.BAD_REQUEST;
          case EMPLACEMENT_INTROUVABLE -> HttpStatus.NOT_FOUND;
          case ANIMAL_IDENTIFIANT_UTILISE -> HttpStatus.CONFLICT;
          case ANIMAL_BOUCLE_UTILISEE -> HttpStatus.CONFLICT;
          case ESPECE_SUIVIE_EN_LOT -> HttpStatus.BAD_REQUEST;
          case PARENT_INCOMPATIBLE -> HttpStatus.BAD_REQUEST;
          case LOT_INTROUVABLE -> HttpStatus.NOT_FOUND;
          case LOT_CLOTURE -> HttpStatus.CONFLICT;
          case ANIMAL_NON_ACTIF -> HttpStatus.CONFLICT;
          case ANIMAL_DEJA_RATTACHE -> HttpStatus.CONFLICT;
          case ESPECE_LOT_INCOMPATIBLE -> HttpStatus.BAD_REQUEST;
          case EVENEMENT_DEJA_ANNULE -> HttpStatus.CONFLICT;
          case PESEE_INVALIDE, CORRECTION_INVALIDE, SAISIE_GROUPEE_INVALIDE ->
              HttpStatus.BAD_REQUEST;
          case CODE_EMAIL_INVALIDE -> HttpStatus.BAD_REQUEST;
          case EMAIL_INDISPONIBLE -> HttpStatus.SERVICE_UNAVAILABLE;
          case LIMITE_EMAIL_ATTEINTE -> HttpStatus.TOO_MANY_REQUESTS;
          case COMPTE_DEJA_INSCRIT -> HttpStatus.CONFLICT;
          case SESSION_INVALIDE -> HttpStatus.UNAUTHORIZED;
          case IDENTIFIANTS_INVALIDES, UTILISATEUR_NON_IDENTIFIE -> HttpStatus.UNAUTHORIZED;
          case LIMITE_CONNEXION_ATTEINTE -> HttpStatus.TOO_MANY_REQUESTS;
          case TRI_INVALIDE -> HttpStatus.BAD_REQUEST;
          case ESPECE_INTROUVABLE,
              EXPLOITATION_INTROUVABLE,
              ALERTE_INTROUVABLE,
              EVENEMENT_INTROUVABLE,
              ANIMAL_INTROUVABLE,
              PROFIL_INTROUVABLE ->
              HttpStatus.NOT_FOUND;
        };
    var erreur = ProblemDetail.forStatusAndDetail(statut, exception.getMessage());
    erreur.setProperty("code", exception.code().name());
    return erreur;
  }
}
