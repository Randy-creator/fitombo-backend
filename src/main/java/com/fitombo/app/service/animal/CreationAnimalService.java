package com.fitombo.app.service.animal;

import com.fitombo.app.dto.AnimalDto;
import com.fitombo.app.dto.animal.CreerAnimalRequest;
import com.fitombo.app.exception.CodeErreur;
import com.fitombo.app.exception.ExceptionApplicative;
import com.fitombo.app.model.cheptel.Animal;
import com.fitombo.app.model.cheptel.SexeAnimal;
import com.fitombo.app.model.referentiel.ModeSuivi;
import com.fitombo.app.repository.AnimalRepository;
import com.fitombo.app.repository.ConflitsPersistance;
import com.fitombo.app.service.ExploitationService;
import com.fitombo.app.service.cheptel.ReferencesCheptel;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreationAnimalService {
  private final AnimalRepository animaux;
  private final ExploitationService exploitations;
  private final ReferencesCheptel references;

  @Transactional(isolation = Isolation.REPEATABLE_READ)
  public AnimalDto creer(UUID utilisateurId, UUID exploitationId, CreerAnimalRequest demande) {
    var exploitation = exploitations.chargerPourGestion(utilisateurId, exploitationId);
    var espece = references.especeActive(demande.especeCode());
    if (espece.getModeSuivi() == ModeSuivi.LOT) {
      throw new ExceptionApplicative(CodeErreur.ESPECE_SUIVIE_EN_LOT);
    }
    var animal =
        new Animal(exploitation, espece, demande.identifiant(), demande.sexe(), demande.origine());
    renseigner(animal, utilisateurId, exploitationId, demande);
    ConflitsPersistance.traduire(
        () -> animaux.saveAndFlush(animal),
        Map.of(
            "uq_animal_identifiant", CodeErreur.ANIMAL_IDENTIFIANT_UTILISE,
            "uq_animal_boucle", CodeErreur.ANIMAL_BOUCLE_UTILISEE));
    return animaux.findVisibleParId(animal.getId(), utilisateurId).orElseThrow();
  }

  private void renseigner(
      Animal animal, UUID utilisateurId, UUID exploitationId, CreerAnimalRequest demande) {
    var race = references.raceFacultative(demande.raceId(), demande.especeCode());
    var emplacement = references.emplacementFacultatif(demande.emplacementId(), exploitationId);
    var mere =
        chercherParent(demande.mereId(), utilisateurId, demande.especeCode(), SexeAnimal.FEMELLE);
    var pere =
        chercherParent(demande.pereId(), utilisateurId, demande.especeCode(), SexeAnimal.MALE);
    animal.renseignerIdentification(
        demande.nom(),
        demande.boucle(),
        demande.marqueTraditionnelle(),
        demande.dateNaissance(),
        Boolean.TRUE.equals(demande.dateNaissanceEstimee()),
        demande.photoUrl());
    animal.renseignerReferences(race, emplacement, mere, pere);
  }

  private Animal chercherParent(UUID id, UUID utilisateurId, String espece, SexeAnimal sexe) {
    if (id == null) {
      return null;
    }
    return animaux
        .findParentVisible(id, utilisateurId, espece, sexe.name())
        .orElseThrow(() -> new ExceptionApplicative(CodeErreur.PARENT_INCOMPATIBLE));
  }
}
