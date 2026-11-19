package com.fitombo.app.controller.lot;

import com.fitombo.app.controller.UtilisateurCourant;
import com.fitombo.app.dto.lot.RattacherAnimalRequest;
import com.fitombo.app.service.lot.RattachementLotService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/lots/{lotId}/animaux")
@RequiredArgsConstructor
public class RattachementLotController {
  private final RattachementLotService rattachements;

  @PostMapping
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void rattacher(
      @UtilisateurCourant UUID utilisateurId,
      @PathVariable UUID lotId,
      @Valid @RequestBody RattacherAnimalRequest demande) {
    rattachements.rattacher(utilisateurId, lotId, demande.animalId());
  }

  @DeleteMapping("/{animalId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void retirer(
      @UtilisateurCourant UUID utilisateurId,
      @PathVariable UUID lotId,
      @PathVariable UUID animalId) {
    rattachements.retirer(utilisateurId, lotId, animalId);
  }
}
