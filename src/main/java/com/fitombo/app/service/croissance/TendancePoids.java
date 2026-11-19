package com.fitombo.app.service.croissance;

import com.fitombo.app.dto.croissance.PointPesee;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;
import java.util.List;

public record TendancePoids(PointPesee derniere, PointPesee precedente) {
  public static TendancePoids depuis(List<PointPesee> pesees) {
    if (pesees.isEmpty()) {
      return new TendancePoids(null, null);
    }
    var derniere = pesees.getLast();
    for (var pesee : pesees.reversed()) {
      if (pesee.date().isBefore(derniere.date())) {
        return new TendancePoids(derniere, pesee);
      }
    }
    return new TendancePoids(derniere, null);
  }

  public BigDecimal gainMoyenQuotidien() {
    if (precedente == null) {
      return null;
    }
    long jours = ChronoUnit.DAYS.between(precedente.date(), derniere.date());
    return gainKg().divide(BigDecimal.valueOf(jours), 6, RoundingMode.HALF_UP);
  }

  public BigDecimal gainKg() {
    return derniere.poidsKg().subtract(precedente.poidsKg());
  }
}
