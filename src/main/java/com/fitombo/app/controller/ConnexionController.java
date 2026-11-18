package com.fitombo.app.controller;

import com.fitombo.app.dto.ConnexionRequest;
import com.fitombo.app.dto.ConnexionResponse;
import com.fitombo.app.service.ConnexionService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ConnexionController {

    private final ConnexionService connexion;

    @PostMapping("/auth/connexion")
    public ResponseEntity<ConnexionResponse> connecter(@Valid @RequestBody ConnexionRequest donnees,
                                                        HttpServletRequest requete) {
        var resultat = connexion.connecter(donnees.telephone(), donnees.motDePasse(), requete.getRemoteAddr());
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(resultat);
    }
}
