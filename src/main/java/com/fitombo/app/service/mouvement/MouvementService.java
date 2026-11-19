package com.fitombo.app.service.mouvement;

import com.fitombo.app.dto.mouvement.CreerMouvementRequest;
import com.fitombo.app.exception.CodeErreur;
import com.fitombo.app.exception.ExceptionApplicative;
import com.fitombo.app.model.cheptel.Propriete;
import com.fitombo.app.model.cheptel.StatutAnimal;
import com.fitombo.app.model.cheptel.StatutLot;
import com.fitombo.app.model.evenement.Mouvement;
import com.fitombo.app.model.evenement.TypeMouvement;
import com.fitombo.app.model.exploitation.Exploitation;
import com.fitombo.app.model.exploitation.Tiers;
import com.fitombo.app.repository.ConflitsPersistance;
import com.fitombo.app.repository.animal.AnimalRepository;
import com.fitombo.app.repository.evenement.EvenementRepository;
import com.fitombo.app.repository.mouvement.ConfiageRepository;
import com.fitombo.app.repository.mouvement.DelaiAttenteRepository;
import com.fitombo.app.service.cheptel.ReferencesCheptel;
import com.fitombo.app.service.cheptel.SortieCheptel;
import com.fitombo.app.service.evenement.CiblesEvenement.Cible;
import com.fitombo.app.service.exploitation.ExploitationService;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MouvementService {
  private final ExploitationService exploitations;
  private final ReferencesCheptel references;
  private final AnimalRepository animaux;
  private final ConfiageRepository confiages;
  private final DelaiAttenteRepository delais;
  private final SortieCheptel sorties;
  private final EvenementRepository evenements;

  public Mouvement creer(Cible cible, CreerMouvementRequest demande) {
    verifierCible(cible, demande);
    Exploitation destination = destination(cible, demande);
    var emplacement =
        destination == null
            ? null
            : references.emplacementFacultatif(
                demande.emplacementDestinationId(), destination.getId());
    var tiers = tiers(cible, demande);
    switch (demande.typeMouvement()) {
      case VENTE ->
          sorties.appliquer(
              cible,
              demande.quantite(),
              demande.dateEvenement(),
              StatutAnimal.VENDU,
              StatutLot.VENDU);
      case ABATTAGE ->
          sorties.appliquer(
              cible,
              demande.quantite(),
              demande.dateEvenement(),
              StatutAnimal.ABATTU,
              StatutLot.ABATTU);
      case CONFIAGE -> confier(cible, demande);
      case RETOUR_CONFIAGE -> terminerConfiage(cible, demande);
      case ACHAT, TRANSFERT -> {}
    }
    return new Mouvement(
        cible.exploitation(),
        cible.animal(),
        cible.lot(),
        demande.dateEvenement(),
        cible.auteur(),
        demande.notes(),
        demande.typeMouvement(),
        demande.quantite(),
        demande.prixTotalAriary(),
        demande.modePrix(),
        demande.poidsTotalKg(),
        demande.numeroPieceCirculation(),
        tiers,
        destination,
        emplacement);
  }

  public LocalDate finaliser(Cible cible, Mouvement mouvement) {
    return switch (mouvement.getTypeMouvement()) {
      case TRANSFERT -> {
        transferer(cible, mouvement);
        ConflitsPersistance.traduire(
            () -> {
              evenements.flush();
              return mouvement;
            },
            Map.of(
                "uq_animal_identifiant", CodeErreur.ANIMAL_IDENTIFIANT_UTILISE,
                "uq_animal_boucle", CodeErreur.ANIMAL_BOUCLE_UTILISEE,
                "uq_lot_code", CodeErreur.LOT_CODE_UTILISE));
        yield null;
      }
      case VENTE, ABATTAGE -> delaiAttente(cible, mouvement);
      default -> null;
    };
  }

  private void verifierCible(Cible cible, CreerMouvementRequest demande) {
    boolean transfert = demande.typeMouvement() == TypeMouvement.TRANSFERT;
    boolean confiage = demande.typeMouvement() == TypeMouvement.CONFIAGE;
    if ((!transfert
            && (demande.exploitationDestinationId() != null
                || demande.emplacementDestinationId() != null))
        || (!confiage && (demande.gardienId() != null || demande.conditions() != null))
        || (cible.animal() != null && demande.quantite() != 1)
        || (cible.animal() == null && demande.quantite() > cible.lot().getEffectifActuel())) {
      throw invalide();
    }
    LocalDate debut =
        cible.animal() == null ? cible.lot().getDateCreation() : cible.animal().getDateNaissance();
    if (debut != null && demande.dateEvenement().isBefore(debut)) {
      throw invalide();
    }
  }

  private Exploitation destination(Cible cible, CreerMouvementRequest demande) {
    if (demande.typeMouvement() != TypeMouvement.TRANSFERT) {
      return null;
    }
    UUID dest = demande.exploitationDestinationId();
    boolean transfertInterne = dest == null || dest.equals(cible.exploitation().getId());
    if ((transfertInterne && demande.emplacementDestinationId() == null)
        || (cible.lot() != null && cible.lot().getEffectifActuel() != demande.quantite())
        || (!transfertInterne
            && cible.lot() != null
            && animaux.existsByLotIdAndDeletedAtIsNull(cible.lot().getId()))) {
      throw invalide();
    }
    if (transfertInterne) {
      return cible.exploitation();
    }
    return exploitations.chargerPourGestion(cible.auteur().getId(), dest);
  }

  private Tiers tiers(Cible cible, CreerMouvementRequest demande) {
    if (demande.nomTiers() == null || demande.nomTiers().isBlank()) {
      return null;
    }
    String type =
        switch (demande.typeMouvement()) {
          case ACHAT -> "FOURNISSEUR";
          case VENTE -> "ACHETEUR";
          case ABATTAGE -> "ABATTOIR";
          default -> "AUTRE";
        };
    return new Tiers(cible.exploitation(), demande.nomTiers(), type);
  }

  private void confier(Cible cible, CreerMouvementRequest demande) {
    if (cible.animal() == null
        || demande.gardienId() == null
        || confiages
            .findByAnimalIdAndTypeAndDateFinIsNull(cible.animal().getId(), "GARDIEN")
            .isPresent()
        || cible.exploitation().getEleveur().getId().equals(demande.gardienId())) {
      throw invalide();
    }
    var gardien = confiages.trouverGardien(demande.gardienId()).orElseThrow(this::invalide);
    confiages.save(
        new Propriete(cible.animal(), gardien, demande.dateEvenement(), demande.conditions()));
  }

  private void terminerConfiage(Cible cible, CreerMouvementRequest demande) {
    if (cible.animal() == null) {
      throw invalide();
    }
    var confiage =
        confiages
            .findByAnimalIdAndTypeAndDateFinIsNull(cible.animal().getId(), "GARDIEN")
            .filter(p -> !demande.dateEvenement().isBefore(p.getDateDebut()))
            .orElseThrow(this::invalide);
    confiage.terminer(demande.dateEvenement());
  }

  private void transferer(Cible cible, Mouvement mouvement) {
    if (cible.animal() != null) {
      cible
          .animal()
          .transferer(
              mouvement.getExploitationDestination(), mouvement.getEmplacementDestination());
    } else {
      cible
          .lot()
          .transferer(
              mouvement.getExploitationDestination(), mouvement.getEmplacementDestination());
    }
  }

  private LocalDate delaiAttente(Cible cible, Mouvement mouvement) {
    UUID animal = cible.animal() == null ? null : cible.animal().getId();
    var lot = cible.animal() == null ? cible.lot() : cible.animal().getLot();
    return delais.finDelai(
        cible.exploitation().getId(),
        animal,
        lot == null ? null : lot.getId(),
        mouvement.getDateEvenement());
  }

  private ExceptionApplicative invalide() {
    return new ExceptionApplicative(CodeErreur.MOUVEMENT_INVALIDE);
  }
}
