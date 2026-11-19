package com.fitombo.app.service.pesee;

import com.fitombo.app.dto.pesee.CreerPeseeRequest;
import com.fitombo.app.exception.CodeErreur;
import com.fitombo.app.exception.ExceptionApplicative;
import com.fitombo.app.model.evenement.MethodePesee;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Set;

final class MesurePesee {
  private static final Set<String> BOVINS = Set.of("ZEBU", "BOVIN_LAITIER");
  private static final BigDecimal COEFFICIENT_RUBAN = BigDecimal.valueOf(80);
  private static final BigDecimal POIDS_MAX_KG = new BigDecimal("999999.999");

  private MesurePesee() {}

  static BigDecimal calculer(CreerPeseeRequest demande, String espece, Integer effectif) {
    verifierEchantillon(demande.nbEchantillon(), effectif);
    if (demande.methode() == MethodePesee.RUBAN) {
      return calculerAuRuban(demande, espece);
    }
    if (demande.poidsKg() == null
        || demande.perimetreThoraciqueCm() != null
        || demande.longueurCm() != null) {
      throw new ExceptionApplicative(CodeErreur.PESEE_INVALIDE);
    }
    return demande.poidsKg();
  }

  private static void verifierEchantillon(Integer taille, Integer effectif) {
    if (effectif == null ? taille != null : taille == null || taille > effectif) {
      throw new ExceptionApplicative(CodeErreur.PESEE_INVALIDE);
    }
  }

  private static BigDecimal calculerAuRuban(CreerPeseeRequest demande, String espece) {
    if (!BOVINS.contains(espece)
        || demande.poidsKg() != null
        || demande.perimetreThoraciqueCm() == null
        || demande.longueurCm() == null) {
      throw new ExceptionApplicative(CodeErreur.PESEE_INVALIDE);
    }
    BigDecimal poids =
        demande
            .perimetreThoraciqueCm()
            .pow(2)
            .multiply(demande.longueurCm())
            .multiply(COEFFICIENT_RUBAN)
            .movePointLeft(6)
            .setScale(3, RoundingMode.HALF_UP);
    if (poids.signum() <= 0 || poids.compareTo(POIDS_MAX_KG) > 0) {
      throw new ExceptionApplicative(CodeErreur.PESEE_INVALIDE);
    }
    return poids;
  }
}
