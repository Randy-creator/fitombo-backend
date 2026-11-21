package com.fitombo.app.dto.finance;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record TransactionDto(
    UUID id,
    String type,
    String categorie,
    BigDecimal montantAriary,
    LocalDate dateTransaction,
    UUID animalId,
    UUID lotId,
    String libelle,
    String modePaiement,
    String notes) {}
