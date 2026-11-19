package com.fitombo.app.dto.production;

import com.fitombo.app.dto.evenement.SaisieEvenement;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreerProductionRequest(
    @NotNull @Pattern(regexp = "PRODUCTION") String type,
    @NotNull UUID exploitationId,
    UUID animalId,
    UUID lotId,
    @NotNull @PastOrPresent LocalDate dateEvenement,
    @Size(max = 1000) String notes,
    @NotNull @Pattern(regexp = "LAIT|OEUFS|FUMIER|MIEL|LAINE|AUTRE") String typeProduction,
    @NotNull @DecimalMin("0") @Digits(integer = 9, fraction = 3) BigDecimal quantite,
    @NotNull @Pattern(regexp = "LITRE|KG|UNITE") String unite)
    implements SaisieEvenement {
  @AssertTrue(message = "Unité incompatible avec la production")
  public boolean isUniteValide() {
    if ("LAIT".equals(typeProduction)) {
      return "LITRE".equals(unite);
    }
    return !"OEUFS".equals(typeProduction)
        || ("UNITE".equals(unite)
            && (quantite == null || quantite.stripTrailingZeros().scale() <= 0));
  }
}
