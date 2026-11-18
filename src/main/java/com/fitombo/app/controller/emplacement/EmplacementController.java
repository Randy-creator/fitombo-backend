package com.fitombo.app.controller.emplacement;

import com.fitombo.app.controller.Pagination;
import com.fitombo.app.controller.UtilisateurCourant;
import com.fitombo.app.dto.PageResult;
import com.fitombo.app.dto.emplacement.EmplacementDto;
import com.fitombo.app.service.emplacement.EmplacementService;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
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
}
