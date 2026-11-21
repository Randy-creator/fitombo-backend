package com.fitombo.app.dto.lot;

import com.fitombo.app.model.cheptel.StatutLot;
import java.time.LocalDate;
import java.util.UUID;

public record FicheLotDto(
    UUID id,
    UUID exploitationId,
    String code,
    String especeCode,
    UUID raceId,
    UUID emplacementId,
    LocalDate dateCreation,
    LocalDate dateCloture,
    Integer effectifInitial,
    Integer effectifActuel,
    StatutLot statut) {}
