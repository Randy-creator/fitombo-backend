package com.fitombo.app.dto.emplacement;

import com.fitombo.app.model.exploitation.TypeEmplacement;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record CreerEmplacementRequest(
    @NotBlank @Size(max = 100) String nom,
    @NotNull TypeEmplacement type,
    @PositiveOrZero Integer capacite) {}
