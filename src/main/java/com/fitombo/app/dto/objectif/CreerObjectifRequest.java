package com.fitombo.app.dto.objectif;

import com.fitombo.app.model.objectif.TypeObjectif;
import com.fitombo.app.model.objectif.UniteObjectif;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreerObjectifRequest(
    UUID animalId,
    UUID lotId,
    @NotNull TypeObjectif type,
    @NotNull UniteObjectif unite,
    @NotNull @DecimalMin("0.001") @Digits(integer = 11, fraction = 3) BigDecimal valeurCible,
    @NotNull @PastOrPresent LocalDate dateDebut,
    @NotNull LocalDate dateCible,
    @Size(max = 1000) String notes) {}
