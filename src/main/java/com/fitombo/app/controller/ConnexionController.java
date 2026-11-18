package com.fitombo.app.controller;

import com.fitombo.app.dto.ConnexionRequest;
import com.fitombo.app.dto.ConnexionResponse;
import com.fitombo.app.service.ConnexionService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ConnexionController {

    private final ConnexionService connexion;
    private final LimiteTentativesConnexion tentatives;

    @PostMapping("/auth/connexion")
    public ResponseEntity<ConnexionResponse> connecter(@Valid @RequestBody ConnexionRequest donnees,
                                                        HttpServletRequest requete) {
        tentatives.verifier(requete.getRemoteAddr());
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(connexion.connecter(donnees.telephone(), donnees.motDePasse()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    ProblemDetail identifiantsInvalides() {
        var erreur = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Identifiants invalides");
        erreur.setProperty("code", "IDENTIFIANTS_INVALIDES");
        return erreur;
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
    ProblemDetail requeteInvalide() {
        var erreur = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Requête de connexion invalide");
        erreur.setProperty("code", "CONNEXION_INVALIDE");
        return erreur;
    }
}
