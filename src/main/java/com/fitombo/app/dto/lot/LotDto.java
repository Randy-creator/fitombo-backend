package com.fitombo.app.dto.lot;

import com.fitombo.app.model.cheptel.StatutLot;
import java.time.LocalDate;
import java.util.UUID;

public record LotDto(
    UUID id,
    String code,
    String especeCode,
    LocalDate dateCreation,
    Integer effectifInitial,
    Integer effectifActuel,
    StatutLot statut) {}
