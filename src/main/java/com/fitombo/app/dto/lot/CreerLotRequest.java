package com.fitombo.app.dto.lot;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;

public record CreerLotRequest(
    @NotBlank @Size(max = 50) String code,
    @NotBlank @Size(max = 30) String especeCode,
    UUID raceId,
    UUID emplacementId,
    @NotNull @PastOrPresent LocalDate dateCreation,
    @NotNull @Positive Integer effectifInitial) {}
