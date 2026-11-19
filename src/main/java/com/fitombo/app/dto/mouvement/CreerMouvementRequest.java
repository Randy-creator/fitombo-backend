package com.fitombo.app.dto.mouvement;

import com.fitombo.app.dto.evenement.SaisieEvenement;
import com.fitombo.app.model.evenement.TypeMouvement;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreerMouvementRequest(
    @NotNull @Pattern(regexp = "MOUVEMENT") String type,
    @NotNull UUID exploitationId,
    UUID animalId,
    UUID lotId,
    @NotNull @PastOrPresent LocalDate dateEvenement,
    @Size(max = 1000) String notes,
    @NotNull TypeMouvement typeMouvement,
    @Positive int quantite,
    @PositiveOrZero @Digits(integer = 12, fraction = 2) BigDecimal prixTotalAriary,
    @Pattern(regexp = "POIDS_VIF|TETE|POIDS_CARCASSE|FORFAIT") String modePrix,
    @DecimalMin("0.01") @Digits(integer = 8, fraction = 2) BigDecimal poidsTotalKg,
    @Size(max = 100) String numeroPieceCirculation,
    @Size(max = 150) String nomTiers,
    UUID exploitationDestinationId,
    UUID emplacementDestinationId,
    UUID gardienId,
    @Size(max = 1000) String conditions)
    implements SaisieEvenement {}
