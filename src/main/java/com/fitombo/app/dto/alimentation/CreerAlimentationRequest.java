package com.fitombo.app.dto.alimentation;

import com.fitombo.app.dto.evenement.SaisieEvenement;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreerAlimentationRequest(
    String type,
    @NotNull UUID exploitationId,
    UUID animalId,
    UUID lotId,
    @NotNull @PastOrPresent LocalDate dateEvenement,
    @Size(max = 1000) String notes,
    @NotBlank @Size(max = 150) String aliment,
    @NotNull @DecimalMin("0.001") @Digits(integer = 7, fraction = 3) BigDecimal quantiteKg,
    @DecimalMin("0") @Digits(integer = 12, fraction = 2) BigDecimal coutAriary)
    implements SaisieEvenement {}
