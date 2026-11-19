package com.fitombo.app.service.pesee;

import com.fitombo.app.dto.pesee.CreerPeseeRequest;
import com.fitombo.app.dto.pesee.PeseeDto;
import com.fitombo.app.exception.CodeErreur;
import com.fitombo.app.exception.ExceptionApplicative;
import com.fitombo.app.model.cheptel.Animal;
import com.fitombo.app.model.cheptel.Lot;
import com.fitombo.app.model.cheptel.StatutAnimal;
import com.fitombo.app.model.cheptel.StatutLot;
import com.fitombo.app.model.evenement.Pesee;
import com.fitombo.app.model.utilisateur.Utilisateur;
import com.fitombo.app.repository.AnimalRepository;
import com.fitombo.app.repository.lot.LotRepository;
import com.fitombo.app.repository.pesee.PeseeRepository;
import com.fitombo.app.service.ExploitationService;
import jakarta.persistence.EntityManager;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PeseeService {
  private final ExploitationService exploitations;
  private final AnimalRepository animaux;
  private final LotRepository lots;
  private final PeseeRepository pesees;
  private final EntityManager entites;

  @Transactional
  public PeseeDto enregistrer(UUID utilisateurId, CreerPeseeRequest demande) {
    var exploitation = exploitations.chargerPourGestion(utilisateurId, demande.exploitationId());
    Animal animal = chargerAnimal(demande.animalId(), exploitation.getId());
    Lot lot = chargerLot(demande.lotId(), exploitation.getId());
    String espece = animal == null ? lot.getEspece().getCode() : animal.getEspece().getCode();
    var poids = MesurePesee.calculer(demande, espece, lot == null ? null : lot.getEffectifActuel());
    var auteur = entites.getReference(Utilisateur.class, utilisateurId);
    var pesee =
        new Pesee(
            exploitation,
            animal,
            lot,
            demande.dateEvenement(),
            auteur,
            demande.notes(),
            demande.methode(),
            poids);
    pesee.renseignerMesures(
        demande.perimetreThoraciqueCm(), demande.longueurCm(), demande.nbEchantillon());
    pesees.saveAndFlush(pesee);
    return new PeseeDto(
        pesee.getId(),
        demande.animalId(),
        demande.lotId(),
        pesee.getDateEvenement(),
        poids,
        pesee.getMethode(),
        pesee.getPerimetreThoraciqueCm(),
        pesee.getLongueurCm(),
        pesee.getNbEchantillon());
  }

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
