package com.fitombo.app.dto.alerte;

import com.fitombo.app.model.alerte.StatutAlerte;
import java.time.LocalDate;
import java.util.UUID;

public record AlerteDto(
    UUID id, String type, LocalDate dateEcheance, StatutAlerte statut, String message) {}
