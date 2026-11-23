package com.fitombo.app.dto.tableaudebord;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record PeriodeTableauDeBord(@NotNull LocalDate du, @NotNull LocalDate au) {
  @AssertTrue(message = "La date de début doit précéder la date de fin")
  public boolean isValide() {
    return du == null || au == null || !du.isAfter(au);
  }
}
