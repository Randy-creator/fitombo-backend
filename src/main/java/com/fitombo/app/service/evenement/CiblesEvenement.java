package com.fitombo.app.service.evenement;

import com.fitombo.app.dto.evenement.SaisieEvenement;
import com.fitombo.app.exception.CodeErreur;
import com.fitombo.app.exception.ExceptionApplicative;
import com.fitombo.app.model.cheptel.Animal;
import com.fitombo.app.model.cheptel.Lot;
import com.fitombo.app.model.cheptel.StatutAnimal;
import com.fitombo.app.model.cheptel.StatutLot;
import com.fitombo.app.model.evenement.Evenement;
import com.fitombo.app.model.exploitation.Exploitation;
import com.fitombo.app.model.utilisateur.Utilisateur;
import com.fitombo.app.repository.animal.AnimalRepository;
import com.fitombo.app.repository.lot.LotRepository;
import com.fitombo.app.service.exploitation.ExploitationService;
import jakarta.persistence.EntityManager;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CiblesEvenement {
  private final ExploitationService exploitations;
  private final AnimalRepository animaux;
  private final LotRepository lots;
  private final EntityManager entites;

  public Cible charger(UUID utilisateurId, SaisieEvenement demande) {
    var exploitation = exploitations.chargerPourGestion(utilisateurId, demande.exploitationId());
    var animal = chargerAnimal(demande.animalId(), exploitation.getId());
    var lot = chargerLot(demande.lotId(), exploitation.getId());
    return new Cible(
        exploitation, animal, lot, entites.getReference(Utilisateur.class, utilisateurId));
  }

  Cible reprendre(UUID utilisateurId, Evenement original) {
    return new Cible(
        original.getExploitation(),
        original.getAnimal(),
        original.getLot(),
        entites.getReference(Utilisateur.class, utilisateurId));
  }

  public record Cible(Exploitation exploitation, Animal animal, Lot lot, Utilisateur auteur) {}

  private Animal chargerAnimal(UUID id, UUID exploitationId) {
    if (id == null) {
      return null;
    }
    Animal animal =
        animaux
            .findByIdAndExploitationIdAndDeletedAtIsNull(id, exploitationId)
            .orElseThrow(() -> new ExceptionApplicative(CodeErreur.ANIMAL_INTROUVABLE));
    if (animal.getStatut() != StatutAnimal.ACTIF) {
      throw new ExceptionApplicative(CodeErreur.ANIMAL_NON_ACTIF);
    }
    return animal;
  }

  private Lot chargerLot(UUID id, UUID exploitationId) {
    if (id == null) {
      return null;
    }
    Lot lot =
        lots.findByIdAndExploitationIdAndDeletedAtIsNull(id, exploitationId)
            .orElseThrow(() -> new ExceptionApplicative(CodeErreur.LOT_INTROUVABLE));
    if (lot.getStatut() != StatutLot.ACTIF) {
      throw new ExceptionApplicative(CodeErreur.LOT_CLOTURE);
    }
    return lot;
  }
}
