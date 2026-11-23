package com.fitombo.app.service.objectif;

import com.fitombo.app.service.croissance.TendancePoids;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

record ProgressionObjectif(
    BigDecimal actuelle,
    BigDecimal pourcentage,
    BigDecimal restant,
    LocalDate prevision,
    String statut,
    String suggestion) {
  private static final BigDecimal CENT = BigDecimal.valueOf(100);

  static ProgressionObjectif calculer(
      BigDecimal actuelle,
      BigDecimal initiale,
      BigDecimal cible,
      LocalDate echeance,
      LocalDate aujourdHui,
      TendancePoids tendance) {
    BigDecimal restant = actuelle == null ? null : cible.subtract(actuelle).max(BigDecimal.ZERO);
    boolean atteint = restant != null && restant.signum() == 0;
    BigDecimal pourcentage = pourcentage(actuelle, initiale, cible, atteint);
    if (atteint) {
      return new ProgressionObjectif(actuelle, pourcentage, restant, null, "ATTEINT", null);
    }
    LocalDate prevision = projeter(restant, tendance);
    if (aujourdHui.isAfter(echeance)) {
      return new ProgressionObjectif(
          actuelle, pourcentage, restant, prevision, "ECHEC", "Réviser la date ou la valeur cible");
    }
    String suggestion =
        prevision != null && prevision.isAfter(echeance)
            ? "Réviser la date ou la valeur cible"
            : null;
    if (suggestion == null
        && tendance != null
        && tendance.precedente() != null
        && tendance.gainKg().signum() <= 0) {
      suggestion = "Réévaluer la cible au vu des dernières pesées";
    }
    return new ProgressionObjectif(
        actuelle, pourcentage, restant, prevision, "EN_COURS", suggestion);
  }

  private static BigDecimal pourcentage(
      BigDecimal actuelle, BigDecimal initiale, BigDecimal cible, boolean atteint) {
    if (actuelle == null) {
      return null;
    }
    if (atteint) {
      return CENT;
    }
    BigDecimal depart =
        initiale == null || initiale.compareTo(cible) >= 0 ? BigDecimal.ZERO : initiale;
    return actuelle
        .subtract(depart)
        .max(BigDecimal.ZERO)
        .multiply(CENT)
        .divide(cible.subtract(depart), 2, RoundingMode.HALF_UP);
  }

  private static LocalDate projeter(BigDecimal restant, TendancePoids tendance) {
    if (restant == null
        || tendance == null
        || tendance.precedente() == null
        || tendance.gainKg().signum() <= 0) {
      return null;
    }
    LocalDate mesure = tendance.derniere().date();
    long intervalle = ChronoUnit.DAYS.between(tendance.precedente().date(), mesure);
    BigDecimal jours =
        restant
            .multiply(BigDecimal.valueOf(intervalle))
            .divide(tendance.gainKg(), 0, RoundingMode.CEILING);
    return jours.compareTo(BigDecimal.valueOf(ChronoUnit.DAYS.between(mesure, LocalDate.MAX))) > 0
        ? null
        : mesure.plusDays(jours.longValueExact());
  }
}
