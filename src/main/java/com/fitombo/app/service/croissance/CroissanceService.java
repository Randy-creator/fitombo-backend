package com.fitombo.app.service.croissance;

import com.fitombo.app.dto.croissance.CourbeCroissanceDto;
import com.fitombo.app.dto.croissance.CourbeCroissanceDto.PointReference;
import com.fitombo.app.dto.croissance.CourbeCroissanceDto.Position;
import com.fitombo.app.dto.croissance.CourbeCroissanceDto.Reference;
import com.fitombo.app.exception.CodeErreur;
import com.fitombo.app.exception.ExceptionApplicative;
import com.fitombo.app.repository.animal.AnimalRepository;
import com.fitombo.app.repository.croissance.CroissanceRepository;
import com.fitombo.app.repository.croissance.ReferenceCroissanceRepository;
import com.fitombo.app.repository.lot.LotRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
public class CroissanceService {
  private final AnimalRepository animaux;
  private final LotRepository lots;
  private final CroissanceRepository croissance;
  private final ReferenceCroissanceRepository references;

  public CourbeCroissanceDto consulter(UUID utilisateur, UUID id, boolean animal) {
    CodeErreur introuvable = animal ? CodeErreur.ANIMAL_INTROUVABLE : CodeErreur.LOT_INTROUVABLE;
    if (!(animal ? animaux.existeVisible(id, utilisateur) : lots.existeVisible(id, utilisateur))) {
      throw new ExceptionApplicative(introuvable);
    }
    var cible =
        references
            .trouverCible(id, animal)
            .orElseThrow(() -> new ExceptionApplicative(introuvable));
    var pesees = croissance.trouverPesees(animal ? id : null, animal ? null : id, LocalDate.now());
    var tendance = TendancePoids.depuis(pesees);
    Reference reference =
        cible.raceId() == null
            ? null
            : references.trouverReference(cible.raceId(), cible.sexe()).orElse(null);
    return new CourbeCroissanceDto(
        cible.origine(),
        pesees,
        tendance.gainMoyenQuotidien(),
        animal ? null : indice(id, cible.effectif(), tendance),
        reference,
        comparer(cible.origine(), tendance, reference));
  }

  private BigDecimal indice(UUID lot, int effectif, TendancePoids tendance) {
    if (effectif <= 0 || tendance.precedente() == null || tendance.gainKg().signum() <= 0) {
      return null;
    }
    if (!croissance.effectifStable(lot, tendance.precedente().date())) {
      return null;
    }
    var quantite =
        croissance.alimentConsomme(lot, tendance.precedente().date(), tendance.derniere().date());
    return quantite == null
        ? null
        : quantite.divide(
            tendance.gainKg().multiply(BigDecimal.valueOf(effectif)), 3, RoundingMode.HALF_UP);
  }

  private Position comparer(LocalDate origine, TendancePoids tendance, Reference reference) {
    if (origine == null || tendance.derniere() == null || reference == null) {
      return null;
    }
    long age = ChronoUnit.DAYS.between(origine, tendance.derniere().date());
    PointReference avant = null;
    for (PointReference point : reference.points()) {
      if (point.ageJours() >= age) {
        return point.ageJours() == age
            ? position(tendance.derniere().poidsKg(), point.poidsMinKg(), point.poidsMaxKg())
            : avant == null
                ? null
                : comparerIntervalle(age, tendance.derniere().poidsKg(), avant, point);
      }
      avant = point;
    }
    return null;
  }

  private Position comparerIntervalle(
      long age, BigDecimal poids, PointReference avant, PointReference apres) {
    BigDecimal part =
        BigDecimal.valueOf(age - avant.ageJours())
            .divide(
                BigDecimal.valueOf(apres.ageJours() - avant.ageJours()), 8, RoundingMode.HALF_UP);
    return position(
        poids,
        interpoler(avant.poidsMinKg(), apres.poidsMinKg(), part),
        interpoler(avant.poidsMaxKg(), apres.poidsMaxKg(), part));
  }

  private BigDecimal interpoler(BigDecimal debut, BigDecimal fin, BigDecimal part) {
    return debut.add(fin.subtract(debut).multiply(part));
  }

  private Position position(BigDecimal poids, BigDecimal min, BigDecimal max) {
    if (poids.compareTo(min) < 0) {
      return Position.EN_RETARD;
    }
    return poids.compareTo(max) > 0 ? Position.EN_AVANCE : Position.DANS_LA_NORME;
  }
}
