package com.fitombo.app.dto.reproduction;

import com.fitombo.app.dto.evenement.SaisieEvenement;
import com.fitombo.app.model.evenement.MethodeSaillie;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;

public record CreerSaillieRequest(
    @NotNull @Pattern(regexp = "SAILLIE") String type,
    @NotNull UUID exploitationId,
    UUID animalId,
    UUID lotId,
    @NotNull @PastOrPresent LocalDate dateEvenement,
    @Size(max = 1000) String notes,
    @NotNull MethodeSaillie methode,
    UUID maleId,
    @Size(max = 200) String maleExterne,
    @Size(max = 100) String referenceSemence)
    implements SaisieEvenement {}
