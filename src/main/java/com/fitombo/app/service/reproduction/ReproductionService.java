package com.fitombo.app.service.reproduction;

import com.fitombo.app.dto.evenement.SaisieEvenement;
import com.fitombo.app.dto.reproduction.CreerDiagnosticGestationRequest;
import com.fitombo.app.dto.reproduction.CreerMiseBasRequest;
import com.fitombo.app.dto.reproduction.CreerSaillieRequest;
import com.fitombo.app.dto.reproduction.NouveauNeRequest;
import com.fitombo.app.exception.CodeErreur;
import com.fitombo.app.exception.ExceptionApplicative;
import com.fitombo.app.model.cheptel.Animal;
import com.fitombo.app.model.cheptel.Lot;
import com.fitombo.app.model.cheptel.OrigineAnimal;
import com.fitombo.app.model.cheptel.SexeAnimal;
import com.fitombo.app.model.evenement.DiagnosticGestation;
import com.fitombo.app.model.evenement.MiseBas;
import com.fitombo.app.model.evenement.Saillie;
import com.fitombo.app.model.referentiel.Espece;
import com.fitombo.app.repository.animal.AnimalRepository;
import com.fitombo.app.repository.reproduction.ReproductionRepository;
import com.fitombo.app.service.evenement.CiblesEvenement.Cible;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReproductionService {
  private final AnimalRepository animaux;
  private final ReproductionRepository reproduction;

  public Saillie saillie(Cible cible, CreerSaillieRequest demande) {
    Espece espece = verifierCible(cible, demande.dateEvenement());
    Animal male = chargerMale(demande.maleId(), cible.exploitation().getId(), espece);
    if (male != null && demande.maleExterne() != null) {
      throw invalide();
    }
    return new Saillie(
        cible.exploitation(),
        cible.animal(),
        cible.lot(),
        demande.dateEvenement(),
        cible.auteur(),
        demande.notes(),
        demande.methode(),
        male,
        demande.maleExterne(),
        demande.referenceSemence(),
        demande.dateEvenement().plusDays(espece.getDureeGestationJours()));
  }

  public DiagnosticGestation diagnostic(Cible cible, CreerDiagnosticGestationRequest demande) {
    verifierCible(cible, demande.dateEvenement());
    Saillie saillie = chargerSaillie(demande, demande.saillieId());
    return new DiagnosticGestation(
        cible.exploitation(),
        cible.animal(),
        cible.lot(),
        demande.dateEvenement(),
        cible.auteur(),
        demande.notes(),
        demande.positif(),
        demande.methode(),
        saillie);
  }

  public MiseBas miseBas(Cible cible, CreerMiseBasRequest demande) {
    verifierCible(cible, demande.dateEvenement());
    Saillie saillie = chargerSaillie(demande, demande.saillieId());
    if ((demande.nesVivants() == 0 && demande.mortsNes() == 0)
        || (saillie != null && reproduction.miseBasExistante(saillie.getId()))) {
      throw invalide();
    }
    var miseBas =
        new MiseBas(
            cible.exploitation(),
            cible.animal(),
            cible.lot(),
            demande.dateEvenement(),
            cible.auteur(),
            demande.notes(),
            demande.nesVivants(),
            demande.mortsNes(),
            Boolean.TRUE.equals(demande.assistance()),
            saillie);
    if (cible.animal() == null) {
      creerLotPetits(cible.lot(), demande, miseBas);
    } else {
      creerPetits(cible.animal(), demande, miseBas, saillie == null ? null : saillie.getMale());
    }
    return miseBas;
  }

  private void creerPetits(Animal mere, CreerMiseBasRequest demande, MiseBas miseBas, Animal pere) {
    List<NouveauNeRequest> petits = demande.petits() == null ? List.of() : demande.petits();
    if (demande.codeLotPetits() != null
        || petits.size() != demande.nesVivants()
        || petits.stream()
                .map(p -> p.identifiant().strip().toLowerCase(Locale.ROOT))
                .distinct()
                .count()
            != petits.size()) {
      throw invalide();
    }
    for (var nouveauNe : petits) {
      var petit =
          new Animal(
              mere.getExploitation(),
              mere.getEspece(),
              nouveauNe.identifiant(),
              nouveauNe.sexe(),
              OrigineAnimal.NE_SUR_PLACE);
      petit.renseignerIdentification(null, null, null, demande.dateEvenement(), false, null);
      petit.renseignerReferences(mere.getRace(), mere.getEmplacement(), mere, pere);
      miseBas.ajouterPetit(petit);
    }
  }

  private void creerLotPetits(Lot parent, CreerMiseBasRequest demande, MiseBas miseBas) {
    String code = demande.codeLotPetits();
    if (demande.petits() != null && !demande.petits().isEmpty()
        || (demande.nesVivants() > 0 && (code == null || code.isBlank()))
        || (demande.nesVivants() == 0 && code != null)) {
      throw invalide();
    }
    if (demande.nesVivants() > 0) {
      miseBas.enregistrerLotPetits(
          new Lot(
              parent.getExploitation(),
              parent.getEspece(),
              parent.getRace(),
              parent.getEmplacement(),
              code,
              demande.dateEvenement(),
              demande.nesVivants()));
    }
  }

  private Espece verifierCible(Cible cible, LocalDate date) {
    Animal animal = cible.animal();
    Espece espece = animal == null ? cible.lot().getEspece() : animal.getEspece();
    LocalDate debut = animal == null ? cible.lot().getDateCreation() : animal.getDateNaissance();
    if (espece.getDureeGestationJours() == null
        || (debut != null && date.isBefore(debut))
        || (animal != null && !SexeAnimal.FEMELLE.name().equals(animal.getSexe()))) {
      throw invalide();
    }
    return espece;
  }

  private Animal chargerMale(UUID id, UUID exploitation, Espece espece) {
    if (id == null) {
      return null;
    }
    return animaux
        .findByIdAndExploitationIdAndDeletedAtIsNull(id, exploitation)
        .filter(male ->
            SexeAnimal.MALE.name().equals(male.getSexe())
                && espece.getCode().equals(male.getEspece().getCode()))
        .orElseThrow(this::invalide);
  }

  private Saillie chargerSaillie(SaisieEvenement demande, UUID id) {
    if (id == null) {
      return null;
    }
    return reproduction
        .findByIdAndStatutAndDeletedAtIsNull(id, "VALIDE")
        .filter(s ->
            s.getExploitation().getId().equals(demande.exploitationId())
                && Objects.equals(
                    demande.animalId(), s.getAnimal() == null ? null : s.getAnimal().getId())
                && Objects.equals(demande.lotId(), s.getLot() == null ? null : s.getLot().getId())
                && !demande.dateEvenement().isBefore(s.getDateEvenement()))
        .orElseThrow(this::invalide);
  }

  private ExceptionApplicative invalide() {
    return new ExceptionApplicative(CodeErreur.REPRODUCTION_INVALIDE);
  }
}
