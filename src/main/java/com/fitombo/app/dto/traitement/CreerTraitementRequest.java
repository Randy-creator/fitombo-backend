package com.fitombo.app.dto.traitement;

import com.fitombo.app.dto.evenement.SaisieEvenement;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;

public record CreerTraitementRequest(
    String type,
    @NotNull UUID exploitationId,
    UUID animalId,
    UUID lotId,
    @NotNull @PastOrPresent LocalDate dateEvenement,
    @Size(max = 1000) String notes,
    @NotBlank @Size(max = 200) String produit,
    @Size(max = 150) String maladie,
    @Size(max = 100) String dose,
    @Positive Integer dureeJours,
    @NotNull @PositiveOrZero Integer delaiAttenteJours)
    implements SaisieEvenement {}
