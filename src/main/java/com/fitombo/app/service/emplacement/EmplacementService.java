package com.fitombo.app.service.emplacement;

import com.fitombo.app.dto.PageResult;
import com.fitombo.app.dto.emplacement.CreerEmplacementRequest;
import com.fitombo.app.dto.emplacement.EmplacementDto;
import com.fitombo.app.exception.CodeErreur;
import com.fitombo.app.exception.ExceptionApplicative;
import com.fitombo.app.model.exploitation.Emplacement;
import com.fitombo.app.repository.ConflitsPersistance;
import com.fitombo.app.repository.emplacement.EmplacementRepository;
import com.fitombo.app.service.exploitation.ExploitationService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
public class EmplacementService {
  private final ExploitationService exploitations;
  private final EmplacementRepository emplacements;

  public PageResult<EmplacementDto> lister(
      UUID utilisateurId, UUID exploitationId, Pageable pageable) {
    exploitations.verifierAcces(utilisateurId, exploitationId);
    return PageResult.from(emplacements.findVisibles(exploitationId, pageable));
  }

  @Transactional(isolation = Isolation.REPEATABLE_READ)
  public EmplacementDto creer(
      UUID utilisateurId, UUID exploitationId, CreerEmplacementRequest demande) {
    var exploitation = exploitations.chargerPourGestion(utilisateurId, exploitationId);
    var emplacement =
        new Emplacement(exploitation, demande.nom(), demande.type(), demande.capacite());
    return sauvegarder(emplacement);
  }

  @Transactional
  public EmplacementDto modifier(UUID utilisateurId, UUID id, CreerEmplacementRequest demande) {
    Emplacement emplacement = charger(utilisateurId, id);
    emplacement.modifier(demande.nom(), demande.type(), demande.capacite());
    return sauvegarder(emplacement);
  }

  @Transactional
  public void archiver(UUID utilisateurId, UUID id) {
    charger(utilisateurId, id).archiver();
  }

  private Emplacement charger(UUID utilisateurId, UUID id) {
    Emplacement emplacement =
        emplacements
            .findByIdAndDeletedAtIsNull(id)
            .orElseThrow(() -> new ExceptionApplicative(CodeErreur.EMPLACEMENT_INTROUVABLE));
    exploitations.chargerPourGestion(utilisateurId, emplacement.getExploitation().getId());
    return emplacement;
  }

  private EmplacementDto sauvegarder(Emplacement emplacement) {
    ConflitsPersistance.traduire(
        () -> emplacements.saveAndFlush(emplacement),
        "uq_emplacement_nom",
        CodeErreur.EMPLACEMENT_NOM_UTILISE);
    return new EmplacementDto(
        emplacement.getId(),
        emplacement.getNom(),
        emplacement.getType(),
        emplacement.getCapacite());
  }
}
