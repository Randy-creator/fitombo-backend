package com.fitombo.app.controller.organisation;

import com.fitombo.app.controller.UtilisateurCourant;
import com.fitombo.app.dto.organisation.ConsentementRequest;
import com.fitombo.app.dto.organisation.PartageDto;
import com.fitombo.app.dto.organisation.StatistiquesOrganisationDto;
import com.fitombo.app.service.organisation.PartageService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PartageController {
  private final PartageService partage;

  @GetMapping("/me/partage-organisation")
  public PartageDto consulter(@UtilisateurCourant UUID utilisateurId) {
    return partage.consulter(utilisateurId);
  }

  @PutMapping("/me/partage-organisation")
  public PartageDto modifier(
      @UtilisateurCourant UUID utilisateurId, @Valid @RequestBody ConsentementRequest demande) {
    return partage.modifier(utilisateurId, demande.partage(), demande.organisationId());
  }

  @GetMapping("/organisation/statistiques")
  public ResponseEntity<StatistiquesOrganisationDto> statistiques(
      @UtilisateurCourant UUID utilisateurId) {
    return ResponseEntity.ok()
        .cacheControl(CacheControl.noStore())
        .body(partage.statistiques(utilisateurId));
  }
}
