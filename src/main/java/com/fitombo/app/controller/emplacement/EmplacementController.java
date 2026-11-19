package com.fitombo.app.controller.emplacement;

import com.fitombo.app.controller.Pagination;
import com.fitombo.app.controller.UtilisateurCourant;
import com.fitombo.app.dto.PageResult;
import com.fitombo.app.dto.emplacement.CreerEmplacementRequest;
import com.fitombo.app.dto.emplacement.EmplacementDto;
import com.fitombo.app.service.emplacement.EmplacementService;
import jakarta.validation.Valid;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/exploitations/{exploitationId}/emplacements")
@RequiredArgsConstructor
public class EmplacementController {
  private final EmplacementService emplacements;

  @GetMapping
  public PageResult<EmplacementDto> lister(
      @UtilisateurCourant UUID utilisateurId,
      @PathVariable UUID exploitationId,
      @PageableDefault(size = 20, sort = "nom") Pageable pageable) {
    Pageable pagination = Pagination.stable(pageable, "id", Set.of("id", "nom", "type"));
    return emplacements.lister(utilisateurId, exploitationId, pagination);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public EmplacementDto creer(
      @UtilisateurCourant UUID utilisateurId,
      @PathVariable UUID exploitationId,
      @Valid @RequestBody CreerEmplacementRequest demande) {
    return emplacements.creer(utilisateurId, exploitationId, demande);
  }
}
