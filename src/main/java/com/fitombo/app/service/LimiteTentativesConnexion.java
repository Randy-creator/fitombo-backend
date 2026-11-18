package com.fitombo.app.service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import com.fitombo.app.config.security.LimiteConnexionProperties;
import com.fitombo.app.exception.CodeErreur;
import com.fitombo.app.exception.ExceptionApplicative;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class LimiteTentativesConnexion {

    private final LimiteConnexionProperties configuration;
    private final Map<String, Integer> tentatives = new HashMap<>();
    private long debutFenetre = System.nanoTime();

    synchronized void enregistrer(String adresse) {
        renouvelerFenetreSiExpiree(System.nanoTime());

        int nombre = tentatives.getOrDefault(adresse, 0);
        if (nombre == 0 && tentatives.size() >= configuration.maxAddressesPerWindow()) {
            throw new ExceptionApplicative(CodeErreur.LIMITE_CONNEXION_ATTEINTE);
        }
        if (nombre >= configuration.maxAttemptsPerAddress()) {
            throw new ExceptionApplicative(CodeErreur.LIMITE_CONNEXION_ATTEINTE);
        }
        tentatives.put(adresse, nombre + 1);
    }

    private void renouvelerFenetreSiExpiree(long maintenant) {
        var tempsEcoule = Duration.ofNanos(maintenant - debutFenetre);
        if (tempsEcoule.compareTo(configuration.window()) >= 0) {
            tentatives.clear();
            debutFenetre = maintenant;
        }
    }
}
