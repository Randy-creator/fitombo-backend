package com.fitombo.app.dto.objectif;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ModifierObjectifRequest(
    @NotNull @DecimalMin("0.001") @Digits(integer = 11, fraction = 3) BigDecimal valeurCible,
    @NotNull LocalDate dateCible,
    @Size(max = 1000) String notes) {}
