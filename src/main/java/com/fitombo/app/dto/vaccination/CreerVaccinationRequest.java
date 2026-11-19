package com.fitombo.app.dto.vaccination;

import com.fitombo.app.dto.evenement.SaisieEvenement;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;

public record CreerVaccinationRequest(
    String type,
    @NotNull UUID exploitationId,
    UUID animalId,
    UUID lotId,
    @NotNull @PastOrPresent LocalDate dateEvenement,
    @Size(max = 1000) String notes,
    @NotBlank @Size(max = 200) String vaccin,
    @Size(max = 150) String maladieCiblee,
    @Size(max = 100) String lotVaccin,
    LocalDate rappelPrevu)
    implements SaisieEvenement {
  @AssertTrue(message = "Le rappel doit suivre la date de vaccination")
  public boolean isRappelValide() {
    return rappelPrevu == null || dateEvenement == null || rappelPrevu.isAfter(dateEvenement);
  }
}
