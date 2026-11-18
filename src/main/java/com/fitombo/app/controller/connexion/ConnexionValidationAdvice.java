package com.fitombo.app.controller.connexion;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = ConnexionController.class)
class ConnexionValidationAdvice {

  @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
  ProblemDetail requeteInvalide() {
    var erreur =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Requête de connexion invalide");
    erreur.setProperty("code", "CONNEXION_INVALIDE");
    return erreur;
  }
}
