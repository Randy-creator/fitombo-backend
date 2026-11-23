package com.fitombo.app.dto.deces;

import com.fitombo.app.dto.evenement.SaisieEvenement;
import com.fitombo.app.model.evenement.CauseDeces;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;

public record CreerDecesRequest(
    @NotNull @Pattern(regexp = "DECES") String type,
    @NotNull UUID exploitationId,
    UUID animalId,
    UUID lotId,
    @NotNull @PastOrPresent LocalDate dateEvenement,
    @Size(max = 1000) String notes,
    @NotNull CauseDeces cause,
    @Positive int nombre,
    @Size(max = 150) String maladieSuspectee,
    Boolean declarationFaite)
    implements SaisieEvenement {}
