package com.fitombo.app.dto.pesee;

import com.fitombo.app.model.evenement.MethodePesee;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record PeseeDto(
    UUID id,
    UUID animalId,
    UUID lotId,
    LocalDate dateEvenement,
    BigDecimal poidsKg,
    MethodePesee methode,
    BigDecimal perimetreThoraciqueCm,
    BigDecimal longueurCm,
    Integer nbEchantillon) {}
