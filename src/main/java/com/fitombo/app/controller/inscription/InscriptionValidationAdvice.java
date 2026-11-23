package com.fitombo.app.controller.inscription;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = InscriptionController.class)
class InscriptionValidationAdvice {
  @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
  ProblemDetail requeteInvalide() {
    var erreur =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Requête d’inscription invalide");
    erreur.setProperty("code", "INSCRIPTION_INVALIDE");
    return erreur;
  }
}
