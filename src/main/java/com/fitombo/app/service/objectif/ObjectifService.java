package com.fitombo.app.service.objectif;

import com.fitombo.app.dto.PageResult;
import com.fitombo.app.dto.objectif.CreerObjectifRequest;
import com.fitombo.app.dto.objectif.ModifierObjectifRequest;
import com.fitombo.app.dto.objectif.ObjectifDto;
import com.fitombo.app.exception.CodeErreur;
import com.fitombo.app.exception.ExceptionApplicative;
import com.fitombo.app.model.commun.EntiteUuid;
import com.fitombo.app.model.objectif.Objectif;
import com.fitombo.app.model.objectif.TypeObjectif;
import com.fitombo.app.model.objectif.UniteObjectif;
import com.fitombo.app.repository.animal.AnimalRepository;
import com.fitombo.app.repository.croissance.CroissanceRepository;
import com.fitombo.app.repository.lot.LotRepository;
import com.fitombo.app.repository.objectif.MesureObjectifRepository;
import com.fitombo.app.repository.objectif.ObjectifRepository;
import com.fitombo.app.service.croissance.TendancePoids;
import com.fitombo.app.service.exploitation.ExploitationService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ObjectifService {
  private final ExploitationService exploitations;
  private final AnimalRepository animaux;
  private final LotRepository lots;
  private final ObjectifRepository objectifs;
  private final CroissanceRepository croissance;
  private final MesureObjectifRepository mesures;

  @Transactional
  public ObjectifDto creer(UUID auteur, UUID exploitationId, CreerObjectifRequest demande) {
    var exploitation = exploitations.chargerPourGestion(auteur, exploitationId);
    verifier(demande);
    var animal =
        demande.animalId() == null
            ? null
            : animaux
                .findByIdAndExploitationIdAndDeletedAtIsNull(demande.animalId(), exploitationId)
                .orElseThrow(() -> new ExceptionApplicative(CodeErreur.ANIMAL_INTROUVABLE));
    var lot =
        demande.lotId() == null
            ? null
            : lots.findByIdAndExploitationIdAndDeletedAtIsNull(demande.lotId(), exploitationId)
                .orElseThrow(() -> new ExceptionApplicative(CodeErreur.LOT_INTROUVABLE));

    LocalDate aujourdHui = LocalDate.now();
    var tendance =
        tendance(demande.type(), demande.animalId(), demande.lotId(), demande.dateDebut());
    BigDecimal initiale =
        switch (demande.type()) {
          case POIDS_CIBLE -> valeurPesee(tendance);
          case EFFECTIF_CIBLE ->
              mesures.effectif(exploitationId, demande.animalId(), demande.lotId());
          case PRODUCTION_CIBLE, MARGE_CIBLE -> BigDecimal.ZERO;
        };
    Objectif objectif =
        new Objectif(
            exploitation,
            animal,
            lot,
            demande.type(),
            initiale,
            demande.valeurCible(),
            demande.unite(),
            demande.dateDebut(),
            demande.dateCible(),
            demande.notes());
    return mesurer(objectifs.save(objectif), aujourdHui);
  }

  @Transactional(readOnly = true)
  public PageResult<ObjectifDto> lister(UUID auteur, UUID exploitation, Pageable page) {
    exploitations.chargerPourGestion(auteur, exploitation);
    LocalDate date = LocalDate.now();
    return PageResult.from(
        objectifs
            .findByExploitationIdAndDeletedAtIsNull(exploitation, page)
            .map(objectif -> mesurer(objectif, date)));
  }

  @Transactional(readOnly = true)
  public long compterEnCours(UUID auteur, UUID exploitation, LocalDate date) {
    exploitations.chargerPourGestion(auteur, exploitation);
    return objectifs
        .findByExploitationIdAndDeletedAtIsNull(exploitation, Pageable.unpaged())
        .stream()
        .filter(objectif -> !objectif.getDateCible().isBefore(date))
        .filter(objectif -> "EN_COURS".equals(mesurer(objectif, date).statut()))
        .count();
  }

  @Transactional(readOnly = true)
  public ObjectifDto consulter(UUID auteur, UUID id) {
    return mesurer(charger(auteur, id), LocalDate.now());
  }

  @Transactional
  public ObjectifDto modifier(UUID auteur, UUID id, ModifierObjectifRequest demande) {
    var objectif = charger(auteur, id);
    if (demande.dateCible().isBefore(objectif.getDateDebut())
        || (objectif.getType() == TypeObjectif.EFFECTIF_CIBLE
            && demande.valeurCible().stripTrailingZeros().scale() > 0)) {
      throw new ExceptionApplicative(CodeErreur.OBJECTIF_INVALIDE);
    }
    objectif.modifier(demande.valeurCible(), demande.dateCible(), demande.notes());
    return mesurer(objectif, LocalDate.now());
  }

  @Transactional
  public void abandonner(UUID auteur, UUID id) {
    charger(auteur, id).abandonner();
  }

  private Objectif charger(UUID auteur, UUID id) {
    var objectif =
        objectifs
            .findByIdAndDeletedAtIsNull(id)
            .orElseThrow(() -> new ExceptionApplicative(CodeErreur.OBJECTIF_INTROUVABLE));
    exploitations.chargerPourGestion(auteur, objectif.getExploitation().getId());
    return objectif;
  }

  private void verifier(CreerObjectifRequest demande) {
    boolean uniteValide =
        switch (demande.type()) {
          case POIDS_CIBLE ->
              demande.unite() == UniteObjectif.KG
                  && (demande.animalId() != null || demande.lotId() != null);
          case EFFECTIF_CIBLE ->
              demande.unite() == UniteObjectif.TETES
                  && demande.valeurCible().stripTrailingZeros().scale() <= 0;
          case PRODUCTION_CIBLE ->
              demande.unite() == UniteObjectif.LITRE_PAR_JOUR
                  || demande.unite() == UniteObjectif.OEUFS_PAR_JOUR;
          case MARGE_CIBLE ->
              demande.unite() == UniteObjectif.ARIARY || demande.unite() == UniteObjectif.POURCENT;
        };
    if (!uniteValide
        || (demande.animalId() != null && demande.lotId() != null)
        || demande.dateCible().isBefore(demande.dateDebut())) {
      throw new ExceptionApplicative(CodeErreur.OBJECTIF_INVALIDE);
    }
  }

  public boolean estEnEchec(Objectif objectif, LocalDate jour) {
    return "ECHEC".equals(mesurer(objectif, jour).statut());
  }

  private ObjectifDto mesurer(Objectif objectif, LocalDate date) {
    UUID exploitation = objectif.getExploitation().getId();
    UUID animal = id(objectif.getAnimal());
    UUID lot = id(objectif.getLot());
    var tendance = tendance(objectif.getType(), animal, lot, date);
    BigDecimal valeur =
        switch (objectif.getType()) {
          case POIDS_CIBLE -> valeurPesee(tendance);
          case EFFECTIF_CIBLE -> mesures.effectif(exploitation, animal, lot);
          case PRODUCTION_CIBLE ->
              mesures.production(
                  exploitation,
                  animal,
                  lot,
                  objectif.getDateDebut(),
                  date,
                  objectif.getUnite() == UniteObjectif.LITRE_PAR_JOUR ? "LAIT" : "OEUFS");
          case MARGE_CIBLE ->
              mesures.marge(
                  exploitation,
                  animal,
                  lot,
                  objectif.getDateDebut(),
                  date,
                  objectif.getUnite() == UniteObjectif.POURCENT);
        };
    var progression =
        ProgressionObjectif.calculer(
            valeur,
            objectif.getValeurInitiale(),
            objectif.getValeurCible(),
            objectif.getDateCible(),
            date,
            tendance);
    return new ObjectifDto(
        objectif.getId(),
        animal,
        lot,
        objectif.getType(),
        objectif.getUnite(),
        objectif.getValeurInitiale(),
        objectif.getValeurCible(),
        objectif.getDateDebut(),
        objectif.getDateCible(),
        objectif.getNotes(),
        progression.actuelle(),
        progression.pourcentage(),
        progression.restant(),
        progression.prevision(),
        progression.statut(),
        progression.suggestion());
  }

  private UUID id(EntiteUuid entite) {
    return entite == null ? null : entite.getId();
  }

  private TendancePoids tendance(TypeObjectif type, UUID animal, UUID lot, LocalDate date) {
    return type == TypeObjectif.POIDS_CIBLE
        ? TendancePoids.depuis(croissance.trouverPesees(animal, lot, date))
        : null;
  }

  private BigDecimal valeurPesee(TendancePoids tendance) {
    return tendance.derniere() == null ? null : tendance.derniere().poidsKg();
  }
}
