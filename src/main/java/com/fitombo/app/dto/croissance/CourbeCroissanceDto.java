package com.fitombo.app.dto.croissance;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CourbeCroissanceDto(
    LocalDate dateOrigine,
    List<PointPesee> pesees,
    BigDecimal gainMoyenQuotidienKg,
    BigDecimal indiceConsommation,
    Reference reference,
    Position position) {
  public enum Position {
    EN_AVANCE,
    DANS_LA_NORME,
    EN_RETARD
  }

  public record Reference(
      UUID id, String libelle, String source, boolean valide, List<PointReference> points) {}

  public record PointReference(
      int ageJours, BigDecimal poidsAttenduKg, BigDecimal poidsMinKg, BigDecimal poidsMaxKg) {}
}
