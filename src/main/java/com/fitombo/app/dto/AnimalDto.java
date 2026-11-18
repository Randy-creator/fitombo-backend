package com.fitombo.app.dto;

import java.time.LocalDate;
import java.util.UUID;

import com.fitombo.app.model.cheptel.StatutAnimal;

public record AnimalDto(UUID id, String identifiant, String nom, String especeCode, String raceNom,
                        String sexe, LocalDate dateNaissance, StatutAnimal statut) {
}
