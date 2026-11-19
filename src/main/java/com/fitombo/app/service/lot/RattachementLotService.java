package com.fitombo.app.service.lot;

import com.fitombo.app.exception.CodeErreur;
import com.fitombo.app.exception.ExceptionApplicative;
import com.fitombo.app.model.cheptel.Animal;
import com.fitombo.app.model.cheptel.Lot;
import com.fitombo.app.model.cheptel.StatutAnimal;
import com.fitombo.app.model.cheptel.StatutLot;
import com.fitombo.app.repository.AnimalRepository;
import com.fitombo.app.repository.lot.LotRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class RattachementLotService {
  private final LotRepository lots;
  private final AnimalRepository animaux;

  public void rattacher(UUID utilisateurId, UUID lotId, UUID animalId) {
    Lot lot = chargerLot(utilisateurId, lotId);
    Animal animal = chargerAnimal(animalId, lot);
    if (estDansLeLot(animal, lotId)) {
      return;
    }
    verifierRattachement(animal, lot);
    animal.affecterLot(lot);
  }

  public void retirer(UUID utilisateurId, UUID lotId, UUID animalId) {
    Lot lot = chargerLot(utilisateurId, lotId);
    Animal animal = chargerAnimal(animalId, lot);
    if (animal.getLot() == null) {
      return;
    }
    if (!estDansLeLot(animal, lotId)) {
      throw new ExceptionApplicative(CodeErreur.ANIMAL_INTROUVABLE);
    }
    animal.affecterLot(null);
  }

  private void verifierRattachement(Animal animal, Lot lot) {
    if (lot.getStatut() != StatutLot.ACTIF) {
      throw new ExceptionApplicative(CodeErreur.LOT_CLOTURE);
    }
    if (animal.getStatut() != StatutAnimal.ACTIF) {
      throw new ExceptionApplicative(CodeErreur.ANIMAL_NON_ACTIF);
    }
    if (!animal.getEspece().getCode().equals(lot.getEspece().getCode())) {
      throw new ExceptionApplicative(CodeErreur.ESPECE_LOT_INCOMPATIBLE);
    }
    if (animal.getLot() != null) {
      throw new ExceptionApplicative(CodeErreur.ANIMAL_DEJA_RATTACHE);
    }
  }

  private boolean estDansLeLot(Animal animal, UUID lotId) {
    return animal.getLot() != null && lotId.equals(animal.getLot().getId());
  }

  private Lot chargerLot(UUID utilisateurId, UUID lotId) {
    return lots.findPourGestion(lotId, utilisateurId)
        .orElseThrow(() -> new ExceptionApplicative(CodeErreur.LOT_INTROUVABLE));
  }

  private Animal chargerAnimal(UUID animalId, Lot lot) {
    return animaux
        .findByIdAndExploitationIdAndDeletedAtIsNull(animalId, lot.getExploitation().getId())
        .orElseThrow(() -> new ExceptionApplicative(CodeErreur.ANIMAL_INTROUVABLE));
  }
}
