package com.fitombo.app.dto.historique;

import com.fitombo.app.model.evenement.TypeEvenement;
import java.time.LocalDate;
import java.util.UUID;

public record EvenementHistoriqueDto(
    UUID id,
    TypeEvenement type,
    LocalDate dateEvenement,
    String statut,
    String notes,
    UUID corrigeEvenementId) {}
