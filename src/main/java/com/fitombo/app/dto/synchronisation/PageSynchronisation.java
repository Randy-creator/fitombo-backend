package com.fitombo.app.dto.synchronisation;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record PageSynchronisation(
    List<Changement> changements, FenetreSynchronisation curseur, boolean suite) {
  public record Changement(
      String transaction,
      long sequence,
      UUID evenementId,
      boolean retire,
      String type,
      String statut,
      UUID animalId,
      UUID lotId,
      LocalDate dateEvenement,
      String notes,
      UUID corrigeEvenementId) {}
}
