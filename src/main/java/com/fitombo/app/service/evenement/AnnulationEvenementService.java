package com.fitombo.app.service.evenement;

import com.fitombo.app.exception.CodeErreur;
import com.fitombo.app.exception.ExceptionApplicative;
import com.fitombo.app.model.evenement.Evenement;
import com.fitombo.app.repository.evenement.EvenementRepository;
import com.fitombo.app.service.exploitation.ExploitationService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AnnulationEvenementService {
  private final EvenementRepository evenements;
  private final ExploitationService exploitations;

  @Transactional
  public void annuler(UUID utilisateurId, UUID evenementId, String motif) {
    UUID exploitationId =
        evenements.trouverExploitation(evenementId).orElseThrow(this::evenementIntrouvable);
    exploitations.chargerPourGestion(utilisateurId, exploitationId);
    Evenement evenement =
        evenements
            .verrouiller(evenementId)
            .flatMap(evenements::findById)
            .orElseThrow(this::evenementIntrouvable);
    evenement.annuler(motif.strip());
  }

  private ExceptionApplicative evenementIntrouvable() {
    return new ExceptionApplicative(CodeErreur.EVENEMENT_INTROUVABLE);
  }
}
