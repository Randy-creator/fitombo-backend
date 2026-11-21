package com.fitombo.app.controller.membre;

import com.fitombo.app.controller.Pagination;
import com.fitombo.app.controller.UtilisateurCourant;
import com.fitombo.app.dto.PageResult;
import com.fitombo.app.dto.membre.InvitationMembreRequest;
import com.fitombo.app.dto.membre.MembreDto;
import com.fitombo.app.service.membre.MembreService;
import jakarta.validation.Valid;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MembreController {
  private final MembreService membres;

  @GetMapping("/exploitations/{exploitationId}/membres")
  public PageResult<MembreDto> lister(
      @UtilisateurCourant UUID utilisateurId, @PathVariable UUID exploitationId, Pageable page) {
    return membres.lister(utilisateurId, exploitationId, pagination(page));
  }

  @PostMapping("/exploitations/{exploitationId}/membres")
  @ResponseStatus(HttpStatus.CREATED)
  public MembreDto inviter(
      @UtilisateurCourant UUID utilisateurId,
      @PathVariable UUID exploitationId,
      @Valid @RequestBody InvitationMembreRequest demande) {
    return membres.inviter(utilisateurId, exploitationId, demande);
  }

  @DeleteMapping("/exploitations/{exploitationId}/membres/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void revoquer(
      @UtilisateurCourant UUID utilisateurId,
      @PathVariable UUID exploitationId,
      @PathVariable UUID id) {
    membres.revoquer(utilisateurId, exploitationId, id);
  }

  @GetMapping("/me/invitations")
  public PageResult<MembreDto> invitations(@UtilisateurCourant UUID utilisateurId, Pageable page) {
    return membres.invitations(utilisateurId, pagination(page));
  }

  @PostMapping("/me/invitations/{id}/acceptation")
  public MembreDto accepter(@UtilisateurCourant UUID utilisateurId, @PathVariable UUID id) {
    return membres.accepter(utilisateurId, id);
  }

  private Pageable pagination(Pageable page) {
    return Pagination.stable(page, "id", Set.of("id", "dateDebut"));
  }
}
