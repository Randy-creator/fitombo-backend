package com.fitombo.app.dto.finance;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record FiltreMarge(@NotNull LocalDate du, @NotNull LocalDate au, UUID animalId, UUID lotId) {
  @AssertTrue(message = "Vérifier la période et choisir au plus une cible")
  public boolean isValide() {
    return (animalId == null || lotId == null) && (du == null || au == null || !du.isAfter(au));
  }
}
