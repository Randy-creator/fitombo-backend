package com.fitombo.app.controller;

import com.fitombo.app.dto.AnimalDto;
import com.fitombo.app.dto.PageResult;
import com.fitombo.app.model.cheptel.StatutAnimal;
import com.fitombo.app.service.AnimalService;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/exploitations/{exploitationId}/animaux")
@RequiredArgsConstructor
public class AnimalController {

  private final AnimalService animaux;

  @GetMapping
  public PageResult<AnimalDto> lister(
      @UtilisateurCourant UUID utilisateurId,
      @PathVariable UUID exploitationId,
      @RequestParam(required = false) StatutAnimal statut,
      @PageableDefault(size = 20, sort = "identifiant") Pageable pageable) {
    Pageable pagination =
        Pagination.stable(pageable, "id", Set.of("id", "identifiant", "nom", "statut"));
    return animaux.lister(utilisateurId, exploitationId, statut, pagination);
  }
}
