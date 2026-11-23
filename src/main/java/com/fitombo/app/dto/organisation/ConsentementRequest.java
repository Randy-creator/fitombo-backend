package com.fitombo.app.dto.organisation;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ConsentementRequest(@NotNull Boolean partage, UUID organisationId) {
  @AssertTrue(message = "Identifie l’organisation à laquelle tu donnes ton accord")
  public boolean isValide() {
    return !Boolean.TRUE.equals(partage) || organisationId != null;
  }
}
