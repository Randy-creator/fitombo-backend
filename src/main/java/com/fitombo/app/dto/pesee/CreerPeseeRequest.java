package com.fitombo.app.dto.pesee;

import com.fitombo.app.dto.evenement.SaisieEvenement;
import com.fitombo.app.model.evenement.MethodePesee;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreerPeseeRequest(
    @NotNull @Pattern(regexp = "PESEE") String type,
    @NotNull UUID exploitationId,
    UUID animalId,
    UUID lotId,
    @NotNull @PastOrPresent LocalDate dateEvenement,
    @NotNull MethodePesee methode,
    @DecimalMin("0.001") @Digits(integer = 6, fraction = 3) BigDecimal poidsKg,
    @DecimalMin("0.1") @Digits(integer = 5, fraction = 1) BigDecimal perimetreThoraciqueCm,
    @DecimalMin("0.1") @Digits(integer = 5, fraction = 1) BigDecimal longueurCm,
    @Positive Integer nbEchantillon,
    @Size(max = 1000) String notes)
    implements SaisieEvenement {}
