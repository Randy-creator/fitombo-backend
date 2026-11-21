package com.fitombo.app.service.membre;

import com.fitombo.app.dto.PageResult;
import com.fitombo.app.dto.membre.InvitationMembreRequest;
import com.fitombo.app.dto.membre.MembreDto;
import com.fitombo.app.exception.CodeErreur;
import com.fitombo.app.exception.ExceptionApplicative;
import com.fitombo.app.model.exploitation.MembreExploitation;
import com.fitombo.app.model.exploitation.RoleExploitation;
import com.fitombo.app.repository.ConflitsPersistance;
import com.fitombo.app.repository.membre.MembreRepository;
import com.fitombo.app.service.exploitation.ExploitationService;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MembreService {
  private final ExploitationService exploitations;
  private final MembreRepository membres;

  @Transactional(readOnly = true)
  public PageResult<MembreDto> lister(UUID auteur, UUID exploitation, Pageable page) {
    exploitations.chargerPourGestion(auteur, exploitation);
    return PageResult.from(membres.lister(exploitation, page));
  }

  @Transactional
  public MembreDto inviter(UUID auteur, UUID exploitationId, InvitationMembreRequest demande) {
    var exploitation = exploitations.chargerPourGestion(auteur, exploitationId);
    if (auteur.equals(demande.utilisateurId()) || demande.role() == RoleExploitation.PROPRIETAIRE) {
      throw new ExceptionApplicative(CodeErreur.INVITATION_INVALIDE);
    }
    var invite = membres.compteActif(demande.utilisateurId()).orElseThrow(this::introuvable);
    var membre = new MembreExploitation(exploitation, invite, demande.role(), LocalDate.now());
    return dto(
        ConflitsPersistance.traduire(
            () -> membres.saveAndFlush(membre), "uq_membre_actif", CodeErreur.MEMBRE_DEJA_INVITE));
  }

  @Transactional(readOnly = true)
  public PageResult<MembreDto> invitations(UUID auteur, Pageable page) {
    membres.compteActif(auteur).orElseThrow(this::introuvable);
    return PageResult.from(membres.invitations(auteur, page));
  }

  @Transactional
  public MembreDto accepter(UUID auteur, UUID id) {
    membres.compteActif(auteur).orElseThrow(this::introuvable);
    var membre =
        membres
            .findByIdAndUtilisateurId(id, auteur)
            .filter(
                m -> m.getExploitation().getDeletedAt() == null && !"REVOQUE".equals(m.getStatut()))
            .orElseThrow(this::introuvable);
    if ("INVITE".equals(membre.getStatut())) {
      membre.accepter(LocalDate.now());
    }
    return dto(membre);
  }

  @Transactional
  public void revoquer(UUID auteur, UUID exploitation, UUID id) {
    exploitations.chargerPourGestion(auteur, exploitation);
    var membre = membres.findByIdAndExploitationId(id, exploitation).orElseThrow(this::introuvable);
    if (!"REVOQUE".equals(membre.getStatut())) {
      membre.revoquer(LocalDate.now());
    }
  }

  private MembreDto dto(MembreExploitation membre) {
    return new MembreDto(
        membre.getId(),
        membre.getExploitation().getId(),
        membre.getUtilisateur().getId(),
        membre.getRole(),
        membre.getStatut(),
        membre.getDateDebut(),
        membre.getDateFin());
  }

  private ExceptionApplicative introuvable() {
    return new ExceptionApplicative(CodeErreur.MEMBRE_INTROUVABLE);
  }
}
