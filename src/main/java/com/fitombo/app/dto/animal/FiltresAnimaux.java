package com.fitombo.app.dto.animal;

import com.fitombo.app.model.cheptel.SexeAnimal;
import com.fitombo.app.model.cheptel.StatutAnimal;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record FiltresAnimaux(
    @Size(max = 100) String recherche,
    @Size(max = 30) String especeCode,
    UUID emplacementId,
    SexeAnimal sexe,
    StatutAnimal statut,
    @PositiveOrZero Integer ageMinJours,
    @PositiveOrZero Integer ageMaxJours) {

  @AssertTrue(message = "L’âge minimal doit être inférieur ou égal à l’âge maximal")
  public boolean isTrancheAgeValide() {
    return ageMinJours == null || ageMaxJours == null || ageMinJours <= ageMaxJours;
  }
}
