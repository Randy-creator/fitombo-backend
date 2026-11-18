package com.fitombo.app.controller;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
class LimiteTentativesConnexion {

    private static final int TENTATIVES_PAR_ADRESSE = 10;
    private static final int ADRESSES_PAR_FENETRE = 10_000;
    private static final long FENETRE_NANOS = Duration.ofMinutes(1).toNanos();

    private final Map<String, Integer> tentatives = new HashMap<>();
    private long debutFenetre = System.nanoTime();

    synchronized void verifier(String adresse) {
        long maintenant = System.nanoTime();
        if (maintenant - debutFenetre >= FENETRE_NANOS) {
            tentatives.clear();
            debutFenetre = maintenant;
        }
        int nombre = tentatives.getOrDefault(adresse, 0);
        if (nombre >= TENTATIVES_PAR_ADRESSE || (nombre == 0 && tentatives.size() >= ADRESSES_PAR_FENETRE)) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Réessaie dans une minute");
        }
        tentatives.put(adresse, nombre + 1);
    }
}
