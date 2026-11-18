package com.fitombo.app.service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import com.fitombo.app.dto.ConnexionResponse;
import com.fitombo.app.repository.UtilisateurRepository;
import com.fitombo.app.repository.UtilisateurRepository.Identifiants;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class ConnexionService {

    private final UtilisateurRepository utilisateurs;
    private final PasswordEncoder motsDePasse;
    private final JwtEncoder jetons;
    private final String issuer;
    private final Duration duree;
    private final String hashFactice;

    public ConnexionService(UtilisateurRepository utilisateurs, PasswordEncoder motsDePasse, JwtEncoder jetons,
                             @Value("${app.security.jwt.issuer}") String issuer,
                             @Value("${app.security.jwt.access-token-ttl}") Duration duree) {
        this.utilisateurs = utilisateurs;
        this.motsDePasse = motsDePasse;
        this.jetons = jetons;
        this.issuer = issuer;
        if (duree.compareTo(Duration.ofSeconds(1)) < 0 || duree.compareTo(Duration.ofMinutes(30)) > 0) {
            throw new IllegalArgumentException("La durée du JWT d’accès doit être comprise entre 1 seconde et 30 minutes");
        }
        this.duree = duree;
        this.hashFactice = motsDePasse.encode(UUID.randomUUID().toString());
    }

    public ConnexionResponse connecter(String telephone, String motDePasse) {
        if (motDePasse.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new BadCredentialsException("Identifiants invalides");
        }
        var compte = utilisateurs.findIdentifiants(telephone);
        var hash = compte.map(Identifiants::getMotDePasseHash).orElse(hashFactice);
        if (!motsDePasse.matches(motDePasse, hash) || compte.isEmpty()) {
            throw new BadCredentialsException("Identifiants invalides");
        }
        return emettreJeton(compte.orElseThrow());
    }

    private ConnexionResponse emettreJeton(Identifiants compte) {
        Instant maintenant = Instant.now();
        var claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(compte.getId().toString())
                .issuedAt(maintenant)
                .expiresAt(maintenant.plus(duree))
                .claim("role", compte.getRole().name())
                .claim("token_use", "access")
                .build();
        var header = JwsHeader.with(MacAlgorithm.HS256).type("JWT").build();
        var jeton = jetons.encode(JwtEncoderParameters.from(header, claims));
        return new ConnexionResponse(jeton.getTokenValue(), "Bearer", duree.toSeconds());
    }
}
