package com.fitombo.app.dto.historique;

import com.fitombo.app.model.evenement.TypeEvenement;
import jakarta.validation.constraints.AssertTrue;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;

public record FiltresEvenements(
    UUID animalId,
    UUID lotId,
    TypeEvenement type,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate du,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate au) {

  @AssertTrue(message = "Choisir exactement un animal ou un lot")
  public boolean isCibleUnique() {
    return (animalId == null) != (lotId == null);
  }

  @AssertTrue(message = "La date de début doit précéder la date de fin")
  public boolean isPeriodeValide() {
    return du == null || au == null || !du.isAfter(au);
  }

  public static FiltresEvenements pourLot(UUID id) {
    return new FiltresEvenements(null, id, null, null, null);
  }

  public static FiltresEvenements pourAnimal(UUID id) {
    return new FiltresEvenements(id, null, null, null, null);
  }
}
