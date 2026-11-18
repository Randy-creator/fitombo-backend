package com.fitombo.app.dto.profil;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ModifierProfilRequest(
    @NotBlank @Size(max = 150) String nom, @NotNull @Pattern(regexp = "fr|mg") String langue) {}
