package com.fitombo.app.dto.evenement;

import com.fitombo.app.model.evenement.CauseDeces;
import com.fitombo.app.model.evenement.MethodeDiagnostic;
import com.fitombo.app.model.evenement.MethodeSaillie;
import com.fitombo.app.model.evenement.TypeEvenement;
import com.fitombo.app.model.evenement.TypeMouvement;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record EvenementDetailDto(
    UUID id,
    TypeEvenement type,
    UUID animalId,
    UUID lotId,
    LocalDate dateEvenement,
    String notes,
    String statut,
    UUID corrigeEvenementId,
    String motifAnnulation,
    Details details) {
  public sealed interface Details permits Saillie, Diagnostic, Naissance, Mouvement, Deces {}

  public record Saillie(
      MethodeSaillie methode,
      UUID maleId,
      String maleExterne,
      String referenceSemence,
      LocalDate miseBasPrevue)
      implements Details {}

  public record Diagnostic(boolean positif, MethodeDiagnostic methode, UUID saillieId)
      implements Details {}

  public record Naissance(
      int nesVivants,
      int mortsNes,
      boolean assistance,
      UUID saillieId,
      UUID lotPetitsId,
      List<UUID> petitsIds)
      implements Details {}

  public record Mouvement(
      TypeMouvement type,
      int quantite,
      BigDecimal prixTotalAriary,
      String modePrix,
      BigDecimal poidsTotalKg,
      String numeroPieceCirculation,
      String nomTiers,
      UUID exploitationOrigineId,
      UUID exploitationDestinationId,
      UUID emplacementDestinationId)
      implements Details {}

  public record Deces(
      CauseDeces cause, int nombre, String maladieSuspectee, boolean declarationFaite)
      implements Details {}
}
