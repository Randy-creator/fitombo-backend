package com.fitombo.app.dto.evenement;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AnnulerEvenementRequest(@NotBlank @Size(max = 1000) String motif) {}
