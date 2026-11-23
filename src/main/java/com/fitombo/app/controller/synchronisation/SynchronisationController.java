package com.fitombo.app.controller.synchronisation;

import com.fitombo.app.controller.UtilisateurCourant;
import com.fitombo.app.dto.synchronisation.LotSynchronisationRequest;
import com.fitombo.app.dto.synchronisation.ResultatSynchronisation;
import com.fitombo.app.service.synchronisation.SynchronisationService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SynchronisationController {
  private final SynchronisationService synchronisation;

  @PostMapping("/sync/push")
  public List<ResultatSynchronisation> envoyer(
      @UtilisateurCourant UUID utilisateurId,
      @Valid @RequestBody LotSynchronisationRequest demande) {
    return synchronisation.envoyer(utilisateurId, demande);
  }
}
