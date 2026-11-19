package com.fitombo.app.dto.lot;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record RattacherAnimalRequest(@NotNull UUID animalId) {}
