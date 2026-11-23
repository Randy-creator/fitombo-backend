package com.fitombo.app.controller.synchronisation;

import com.fitombo.app.controller.UtilisateurCourant;
import com.fitombo.app.dto.synchronisation.FenetreSynchronisation;
import com.fitombo.app.dto.synchronisation.LotSynchronisationRequest;
import com.fitombo.app.dto.synchronisation.PageSynchronisation;
import com.fitombo.app.dto.synchronisation.ResultatSynchronisation;
import com.fitombo.app.service.synchronisation.SynchronisationService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SynchronisationController {
  private final SynchronisationService synchronisation;

  @GetMapping("/sync/pull")
  public ResponseEntity<PageSynchronisation> recevoir(
      @UtilisateurCourant UUID utilisateurId,
      @RequestParam UUID exploitationId,
      @Valid @ModelAttribute FenetreSynchronisation curseur) {
    return ResponseEntity.ok()
        .cacheControl(CacheControl.noStore())
        .body(synchronisation.recevoir(utilisateurId, exploitationId, curseur));
  }

  @PostMapping("/sync/push")
  public List<ResultatSynchronisation> envoyer(
      @UtilisateurCourant UUID utilisateurId,
      @Valid @RequestBody LotSynchronisationRequest demande) {
    return synchronisation.envoyer(utilisateurId, demande);
  }
}
