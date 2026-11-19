package com.fitombo.app.dto.reproduction;

import com.fitombo.app.model.cheptel.SexeAnimal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record NouveauNeRequest(
    @NotBlank @Size(max = 50) String identifiant, @NotNull SexeAnimal sexe) {}
