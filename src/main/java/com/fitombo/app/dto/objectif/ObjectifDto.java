package com.fitombo.app.dto.objectif;

import com.fitombo.app.model.objectif.TypeObjectif;
import com.fitombo.app.model.objectif.UniteObjectif;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ObjectifDto(
    UUID id,
    UUID animalId,
    UUID lotId,
    TypeObjectif type,
    UniteObjectif unite,
    BigDecimal valeurInitiale,
    BigDecimal valeurCible,
    LocalDate dateDebut,
    LocalDate dateCible,
    String notes,
    BigDecimal valeurActuelle,
    BigDecimal progressionPourcent,
    BigDecimal gainRestant,
    LocalDate datePrevisionnelle,
    String statut,
    String suggestion) {}
