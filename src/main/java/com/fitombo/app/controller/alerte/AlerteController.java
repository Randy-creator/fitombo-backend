package com.fitombo.app.controller.alerte;

import com.fitombo.app.controller.Pagination;
import com.fitombo.app.controller.UtilisateurConnecte;
import com.fitombo.app.dto.PageResult;
import com.fitombo.app.dto.alerte.AlerteDto;
import com.fitombo.app.dto.alerte.MarquerAlerteRequest;
import com.fitombo.app.model.alerte.StatutAlerte;
import com.fitombo.app.service.alerte.AlerteService;
import jakarta.validation.Valid;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/alertes")
@RequiredArgsConstructor
public class AlerteController {
  private final AlerteService alertes;

  @GetMapping
  public PageResult<AlerteDto> lister(
      @AuthenticationPrincipal Jwt jwt,
      @RequestParam(required = false) StatutAlerte statut,
      @PageableDefault(size = 20, sort = "dateEcheance") Pageable pageable) {
    UUID utilisateurId = UtilisateurConnecte.id(jwt);
    Pageable pagination = Pagination.stable(pageable, "id", Set.of("id", "dateEcheance", "statut"));
    return alertes.lister(utilisateurId, statut, pagination);
  }

  @PatchMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void marquerVue(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable UUID id,
      @Valid @RequestBody MarquerAlerteRequest demande) {
    alertes.marquerVue(UtilisateurConnecte.id(jwt), id);
  }
}
