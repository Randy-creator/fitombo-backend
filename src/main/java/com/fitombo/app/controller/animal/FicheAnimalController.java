package com.fitombo.app.controller.animal;

import com.fitombo.app.controller.UtilisateurCourant;
import com.fitombo.app.dto.AnimalDto;
import com.fitombo.app.service.AnimalService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class FicheAnimalController {
  private final AnimalService animaux;

  @GetMapping("/animaux/{id}")
  public AnimalDto consulter(@UtilisateurCourant UUID utilisateurId, @PathVariable UUID id) {
    return animaux.consulter(utilisateurId, id);
  }
}
