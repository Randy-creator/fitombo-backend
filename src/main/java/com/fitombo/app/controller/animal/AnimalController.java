package com.fitombo.app.controller.animal;

import com.fitombo.app.controller.Pagination;
import com.fitombo.app.controller.UtilisateurCourant;
import com.fitombo.app.dto.PageResult;
import com.fitombo.app.dto.animal.AnimalDto;
import com.fitombo.app.dto.animal.CreerAnimalRequest;
import com.fitombo.app.dto.animal.FiltresAnimaux;
import com.fitombo.app.dto.croissance.CourbeCroissanceDto;
import com.fitombo.app.service.animal.AnimalService;
import com.fitombo.app.service.animal.CreationAnimalService;
import com.fitombo.app.service.croissance.CroissanceService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AnimalController {

  private final CroissanceService croissance;
  private final AnimalService animaux;
  private final CreationAnimalService creation;

  @GetMapping("/exploitations/{exploitationId}/animaux")
  public PageResult<AnimalDto> lister(
      @UtilisateurCourant UUID utilisateurId,
      @PathVariable UUID exploitationId,
      @Valid @ModelAttribute FiltresAnimaux filtres,
      @PageableDefault(size = 20, sort = "identifiant") Pageable pageable) {
    Pageable pagination =
        Pagination.stable(pageable, "id", Set.of("id", "identifiant", "nom", "statut"));
    return animaux.lister(utilisateurId, exploitationId, filtres, pagination);
  }

  @PostMapping("/exploitations/{exploitationId}/animaux")
  public ResponseEntity<AnimalDto> creer(
      @UtilisateurCourant UUID utilisateurId,
      @PathVariable UUID exploitationId,
      @Valid @RequestBody CreerAnimalRequest demande) {
    var animal = creation.creer(utilisateurId, exploitationId, demande);
    return ResponseEntity.created(URI.create("/animaux/" + animal.id())).body(animal);
  }

  @GetMapping("/animaux/{id}")
  public AnimalDto consulter(@UtilisateurCourant UUID utilisateurId, @PathVariable UUID id) {
    return animaux.consulter(utilisateurId, id);
  }

  @GetMapping("/animaux/{id}/croissance")
  public CourbeCroissanceDto croissance(
      @UtilisateurCourant UUID utilisateurId, @PathVariable UUID id) {
    return croissance.consulter(utilisateurId, id, true);
  }
}
