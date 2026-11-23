package com.fitombo.app.service.evenement;

import com.fitombo.app.dto.evenement.SaisieEvenement;
import com.fitombo.app.dto.pesee.CreerPeseeRequest;
import com.fitombo.app.exception.CodeErreur;
import com.fitombo.app.exception.ExceptionApplicative;
import com.fitombo.app.model.commun.EntiteUuid;
import com.fitombo.app.model.evenement.Evenement;
import com.fitombo.app.model.evenement.Saillie;
import com.fitombo.app.repository.alerte.AlerteRepository;
import com.fitombo.app.repository.evenement.EvenementRepository;
import com.fitombo.app.repository.reproduction.ReproductionRepository;
import com.fitombo.app.service.exploitation.ExploitationService;
import com.fitombo.app.service.pesee.PeseeService;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RevisionEvenementService {
  private final EvenementRepository evenements;
  private final ExploitationService exploitations;

  private final CiblesEvenement cibles;
  private final PeseeService pesees;
  private final SaisieEvenementService saisies;
  private final ReproductionRepository reproduction;
  private final AlerteRepository alertes;

  @Transactional
  public Object corriger(UUID utilisateurId, UUID id, String motif, SaisieEvenement demande) {
    Evenement original = charger(utilisateurId, id);
    verifierRevision(original);
    verifierCorrection(demande, original);
    var cible = cibles.reprendre(utilisateurId, original);
    annulerEtIgnorerAlertes(original, motif);
    if (demande instanceof CreerPeseeRequest pesee) {
      return pesees.enregistrer(cible, pesee, original);
    }
    return saisies.enregistrer(cible, demande, original);
  }

  private void verifierCorrection(SaisieEvenement demande, Evenement original) {
    if (!original.estValide()) {
      throw new ExceptionApplicative(CodeErreur.EVENEMENT_DEJA_ANNULE);
    }
    if (!original.getTypeEvenement().name().equals(demande.type())
        || !original.getExploitation().getId().equals(demande.exploitationId())
        || !Objects.equals(idDe(original.getAnimal()), demande.animalId())
        || !Objects.equals(idDe(original.getLot()), demande.lotId())) {
      throw new ExceptionApplicative(CodeErreur.CORRECTION_INVALIDE);
    }
  }

  private UUID idDe(EntiteUuid cible) {
    return cible == null ? null : cible.getId();
  }

  @Transactional
  public void annuler(UUID utilisateurId, UUID evenementId, String motif) {
    Evenement evenement = charger(utilisateurId, evenementId);
    verifierRevision(evenement);
    annulerEtIgnorerAlertes(evenement, motif);
  }

  private void verifierRevision(Evenement evenement) {
    if (evenement.modifieCheptel()
        || (evenement instanceof Saillie
            && (reproduction.miseBasExistante(evenement.getId())
                || reproduction.diagnosticExistant(evenement.getId())))) {
      throw new ExceptionApplicative(CodeErreur.REVISION_AVEC_EFFETS);
    }
  }

  private void annulerEtIgnorerAlertes(Evenement evenement, String motif) {
    evenement.annuler(motif.strip());
    alertes.ignorerLieesAEvenement(evenement.getId());
  }

  private Evenement charger(UUID utilisateurId, UUID evenementId) {
    UUID exploitationId =
        evenements.trouverExploitation(evenementId).orElseThrow(this::evenementIntrouvable);
    exploitations.chargerPourGestion(utilisateurId, exploitationId);
    return evenements
        .verrouiller(evenementId)
        .flatMap(evenements::findById)
        .orElseThrow(this::evenementIntrouvable);
  }

  private ExceptionApplicative evenementIntrouvable() {
    return new ExceptionApplicative(CodeErreur.EVENEMENT_INTROUVABLE);
  }
}
