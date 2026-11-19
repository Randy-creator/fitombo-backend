package com.fitombo.app.controller.lot;

import com.fitombo.app.controller.Pagination;
import com.fitombo.app.controller.UtilisateurCourant;
import com.fitombo.app.dto.PageResult;
import com.fitombo.app.dto.croissance.CourbeCroissanceDto;
import com.fitombo.app.dto.lot.CreerLotRequest;
import com.fitombo.app.dto.lot.LotDto;
import com.fitombo.app.dto.lot.RattacherAnimalRequest;
import com.fitombo.app.model.cheptel.StatutLot;
import com.fitombo.app.service.croissance.CroissanceService;
import com.fitombo.app.service.lot.LotService;
import com.fitombo.app.service.lot.RattachementLotService;
import jakarta.validation.Valid;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class LotController {
  private final CroissanceService croissance;
  private final LotService lots;
  private final RattachementLotService rattachements;

  @GetMapping("/exploitations/{exploitationId}/lots")
  public PageResult<LotDto> lister(
      @UtilisateurCourant UUID utilisateurId,
      @PathVariable UUID exploitationId,
      @RequestParam(required = false) StatutLot statut,
      @PageableDefault(size = 20, sort = "code") Pageable pageable) {
    Pageable pagination =
        Pagination.stable(pageable, "id", Set.of("id", "code", "statut", "dateCreation"));
    return lots.lister(utilisateurId, exploitationId, statut, pagination);
  }

  @PostMapping("/exploitations/{exploitationId}/lots")
  @ResponseStatus(HttpStatus.CREATED)
  public LotDto creer(
      @UtilisateurCourant UUID utilisateurId,
      @PathVariable UUID exploitationId,
      @Valid @RequestBody CreerLotRequest demande) {
    return lots.creer(utilisateurId, exploitationId, demande);
  }

  @PostMapping("/lots/{lotId}/animaux")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void rattacher(
      @UtilisateurCourant UUID utilisateurId,
      @PathVariable UUID lotId,
      @Valid @RequestBody RattacherAnimalRequest demande) {
    rattachements.rattacher(utilisateurId, lotId, demande.animalId());
  }

  @DeleteMapping("/lots/{lotId}/animaux/{animalId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void retirer(
      @UtilisateurCourant UUID utilisateurId,
      @PathVariable UUID lotId,
      @PathVariable UUID animalId) {
    rattachements.retirer(utilisateurId, lotId, animalId);
  }

  @GetMapping("/lots/{id}/croissance")
  public CourbeCroissanceDto croissance(
      @UtilisateurCourant UUID utilisateurId, @PathVariable UUID id) {
    return croissance.consulter(utilisateurId, id, false);
  }
}
