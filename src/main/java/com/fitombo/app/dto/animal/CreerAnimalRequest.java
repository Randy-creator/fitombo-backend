package com.fitombo.app.dto.animal;

import com.fitombo.app.model.cheptel.OrigineAnimal;
import com.fitombo.app.model.cheptel.SexeAnimal;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;
import org.hibernate.validator.constraints.URL;

public record CreerAnimalRequest(
    @NotBlank @Size(max = 50) String identifiant,
    @Size(max = 50) String boucle,
    @Size(max = 100) String marqueTraditionnelle,
    @Size(max = 100) String nom,
    @NotBlank @Size(max = 30) String especeCode,
    UUID raceId,
    UUID emplacementId,
    @NotNull SexeAnimal sexe,
    @PastOrPresent LocalDate dateNaissance,
    Boolean dateNaissanceEstimee,
    @NotNull OrigineAnimal origine,
    UUID mereId,
    UUID pereId,
    @Size(max = 500) @URL(protocol = "https") String photoUrl) {

  @AssertTrue(message = "Une date estimée doit être renseignée")
  public boolean isDateEstimeeRenseignee() {
    return !Boolean.TRUE.equals(dateNaissanceEstimee) || dateNaissance != null;
  }
}
