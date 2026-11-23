package com.fitombo.app.dto.evenement;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CorrigerEvenementRequest(
    @NotBlank @Size(max = 1000) String motif, @NotNull @Valid SaisieEvenement saisie) {}
