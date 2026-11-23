package com.fitombo.app.service.evenement;

import com.fitombo.app.dto.evenement.EnregistrerEvenementsGroupesRequest;
import com.fitombo.app.dto.evenement.EvenementSaisiDto;
import com.fitombo.app.dto.evenement.SaisieEvenement;
import com.fitombo.app.dto.traitement.CreerTraitementRequest;
import com.fitombo.app.dto.vaccination.CreerVaccinationRequest;
import com.fitombo.app.exception.CodeErreur;
import com.fitombo.app.exception.ExceptionApplicative;
import com.fitombo.app.service.evenement.CiblesEvenement.Cible;
import jakarta.validation.Validator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SaisieGroupeeService {
  private final CiblesEvenement cibles;
  private final SaisieEvenementService saisies;
  private final Validator validateur;

  @Transactional
  public List<EvenementSaisiDto> enregistrer(
      UUID utilisateurId, EnregistrerEvenementsGroupesRequest demande) {
    var modele = demande.saisie();
    verifierCibles(demande, modele);
    var saisiesPreparees =
        demande.animalIds().stream()
            .sorted()
            .map(id -> preparer(utilisateurId, modele, id))
            .toList();
    return saisiesPreparees.stream()
        .map(saisie -> saisies.enregistrer(saisie.cible(), saisie.demande(), null))
        .toList();
  }

  private SaisiePreparee preparer(UUID utilisateurId, SaisieEvenement modele, UUID id) {
    SaisieEvenement demande =
        switch (modele) {
          case CreerVaccinationRequest vaccination -> vaccination.pourAnimal(id);
          case CreerTraitementRequest traitement -> traitement.pourAnimal(id);
          default -> throw new ExceptionApplicative(CodeErreur.SAISIE_GROUPEE_INVALIDE);
        };
    if (!validateur.validate(demande).isEmpty()) {
      throw new ExceptionApplicative(CodeErreur.SAISIE_GROUPEE_INVALIDE);
    }
    return new SaisiePreparee(cibles.charger(utilisateurId, demande), demande);
  }

  private void verifierCibles(EnregistrerEvenementsGroupesRequest demande, SaisieEvenement modele) {
    if (modele.animalId() != null
        || modele.lotId() != null
        || demande.animalIds().stream().distinct().count() != demande.animalIds().size()) {
      throw new ExceptionApplicative(CodeErreur.SAISIE_GROUPEE_INVALIDE);
    }
  }

  private record SaisiePreparee(Cible cible, SaisieEvenement demande) {}
}
