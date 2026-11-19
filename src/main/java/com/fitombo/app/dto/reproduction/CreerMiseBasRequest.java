package com.fitombo.app.dto.reproduction;

import com.fitombo.app.dto.evenement.SaisieEvenement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CreerMiseBasRequest(
    @NotNull @Pattern(regexp = "MISE_BAS") String type,
    @NotNull UUID exploitationId,
    UUID animalId,
    UUID lotId,
    @NotNull @PastOrPresent LocalDate dateEvenement,
    @Size(max = 1000) String notes,
    @PositiveOrZero int nesVivants,
    @PositiveOrZero int mortsNes,
    Boolean assistance,
    UUID saillieId,
    @Size(max = 100) List<@NotNull @Valid NouveauNeRequest> petits,
    @Size(max = 50) String codeLotPetits)
    implements SaisieEvenement {}
