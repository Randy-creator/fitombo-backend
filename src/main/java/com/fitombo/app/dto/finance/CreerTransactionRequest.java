package com.fitombo.app.dto.finance;

import com.fitombo.app.model.finance.CategorieTransaction;
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

public record CreerTransactionRequest(
    @NotNull CategorieTransaction categorie,
    @NotNull @DecimalMin("0.01") @Digits(integer = 12, fraction = 2) BigDecimal montantAriary,
    @NotNull @PastOrPresent LocalDate dateTransaction,
    UUID animalId,
    UUID lotId,
    @Size(max = 200) String libelle,
    @Pattern(regexp = "ESPECES|MVOLA|ORANGE_MONEY|AIRTEL_MONEY|VIREMENT|CHEQUE|CREDIT|AUTRE")
        String modePaiement,
    @Size(max = 1000) String notes) {

  @AssertTrue(message = "Choisir au plus une cible")
  public boolean isCibleValide() {
    return animalId == null || lotId == null;
  }
}
