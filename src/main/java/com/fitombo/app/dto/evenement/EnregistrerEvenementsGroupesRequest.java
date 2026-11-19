package com.fitombo.app.dto.evenement;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public record EnregistrerEvenementsGroupesRequest(
    @NotEmpty @Size(max = 100) List<@NotNull UUID> animalIds, @NotNull SaisieEvenement saisie) {}
