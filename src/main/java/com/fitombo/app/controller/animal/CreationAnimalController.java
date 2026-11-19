package com.fitombo.app.controller.animal;

import com.fitombo.app.controller.UtilisateurCourant;
import com.fitombo.app.dto.AnimalDto;
import com.fitombo.app.dto.animal.CreerAnimalRequest;
import com.fitombo.app.service.animal.CreationAnimalService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CreationAnimalController {
  private final CreationAnimalService animaux;

  @PostMapping("/exploitations/{id}/animaux")
  public ResponseEntity<AnimalDto> creer(
      @UtilisateurCourant UUID utilisateurId,
      @PathVariable UUID id,
      @Valid @RequestBody CreerAnimalRequest demande) {
    var animal = animaux.creer(utilisateurId, id, demande);
    return ResponseEntity.created(URI.create("/animaux/" + animal.id())).body(animal);
  }
}
