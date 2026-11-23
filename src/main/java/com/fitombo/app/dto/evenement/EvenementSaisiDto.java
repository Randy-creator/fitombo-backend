package com.fitombo.app.dto.evenement;

import com.fitombo.app.model.evenement.TypeEvenement;
import java.time.LocalDate;
import java.util.UUID;

public record EvenementSaisiDto(
    UUID id,
    TypeEvenement type,
    UUID animalId,
    UUID lotId,
    LocalDate dateEvenement,
    @com.fasterxml.jackson.annotation.JsonInclude(
            com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
        LocalDate delaiAttenteJusquAu) {}
