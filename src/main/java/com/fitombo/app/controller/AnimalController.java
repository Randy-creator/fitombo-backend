package com.fitombo.app.controller;

import com.fitombo.app.dto.AnimalDto;
import com.fitombo.app.dto.PageResult;
import com.fitombo.app.dto.animal.FiltresAnimaux;
import com.fitombo.app.service.AnimalService;
import jakarta.validation.Valid;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
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
      @Valid @ModelAttribute FiltresAnimaux filtres,
      @PageableDefault(size = 20, sort = "identifiant") Pageable pageable) {
    Pageable pagination =
        Pagination.stable(pageable, "id", Set.of("id", "identifiant", "nom", "statut"));
    return animaux.lister(utilisateurId, exploitationId, filtres, pagination);
  }
}
