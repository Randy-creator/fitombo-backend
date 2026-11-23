package com.fitombo.app.dto.reproduction;

import com.fitombo.app.dto.evenement.SaisieEvenement;
import com.fitombo.app.model.evenement.MethodeDiagnostic;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;

public record CreerDiagnosticGestationRequest(
    @NotNull @Pattern(regexp = "DIAGNOSTIC_GESTATION") String type,
    @NotNull UUID exploitationId,
    UUID animalId,
    UUID lotId,
    @NotNull @PastOrPresent LocalDate dateEvenement,
    @Size(max = 1000) String notes,
    @NotNull Boolean positif,
    @NotNull MethodeDiagnostic methode,
    UUID saillieId)
    implements SaisieEvenement {}
