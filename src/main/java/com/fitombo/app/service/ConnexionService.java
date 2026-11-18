package com.fitombo.app.service;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import com.fitombo.app.dto.ConnexionResponse;
import com.fitombo.app.exception.CodeErreur;
import com.fitombo.app.exception.ExceptionApplicative;
import com.fitombo.app.repository.UtilisateurRepository;
import com.fitombo.app.repository.UtilisateurRepository.Identifiants;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class ConnexionService {

    private static final int LIMITE_OCTETS_BCRYPT = 72;

    private final UtilisateurRepository utilisateurs;
    private final PasswordEncoder motsDePasse;
    private final JetonAccesService jetons;
    private final LimiteTentativesConnexion tentatives;
    private final String hashFactice;

    ConnexionService(UtilisateurRepository utilisateurs, PasswordEncoder motsDePasse, JetonAccesService jetons,
                     LimiteTentativesConnexion tentatives) {
        this.utilisateurs = utilisateurs;
        this.motsDePasse = motsDePasse;
        this.jetons = jetons;
        this.tentatives = tentatives;
        this.hashFactice = motsDePasse.encode(UUID.randomUUID().toString());
    }

    public ConnexionResponse connecter(String telephone, String motDePasse, String adresse) {
        tentatives.enregistrer(adresse);
        Identifiants compte = authentifier(telephone, motDePasse);
        return jetons.emettre(compte.getId(), compte.getRole());
    }

    private Identifiants authentifier(String telephone, String motDePasse) {
        if (motDePasse.getBytes(StandardCharsets.UTF_8).length > LIMITE_OCTETS_BCRYPT) {
            throw new ExceptionApplicative(CodeErreur.IDENTIFIANTS_INVALIDES);
        }

        var compte = utilisateurs.findIdentifiants(telephone);
        String hash = compte.map(Identifiants::getMotDePasseHash).orElse(hashFactice);
        if (!motsDePasse.matches(motDePasse, hash) || compte.isEmpty()) {
            throw new ExceptionApplicative(CodeErreur.IDENTIFIANTS_INVALIDES);
        }
        return compte.orElseThrow();
    }
}
