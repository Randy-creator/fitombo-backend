package com.fitombo.app.controller.lot;

import com.fitombo.app.controller.Pagination;
import com.fitombo.app.controller.UtilisateurConnecte;
import com.fitombo.app.dto.PageResult;
import com.fitombo.app.dto.lot.LotDto;
import com.fitombo.app.model.cheptel.StatutLot;
import com.fitombo.app.service.lot.LotService;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/exploitations/{exploitationId}/lots")
@RequiredArgsConstructor
public class LotController {
  private final LotService lots;

  @GetMapping
  public PageResult<LotDto> lister(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable UUID exploitationId,
      @RequestParam(required = false) StatutLot statut,
      @PageableDefault(size = 20, sort = "code") Pageable pageable) {
    UUID utilisateurId = UtilisateurConnecte.id(jwt);
    Pageable pagination =
        Pagination.stable(pageable, "id", Set.of("id", "code", "statut", "dateCreation"));
    return lots.lister(utilisateurId, exploitationId, statut, pagination);
  }
}
