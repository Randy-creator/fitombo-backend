package com.fitombo.app.controller.objectif;

import com.fitombo.app.controller.Pagination;
import com.fitombo.app.controller.UtilisateurCourant;
import com.fitombo.app.dto.PageResult;
import com.fitombo.app.dto.objectif.CreerObjectifRequest;
import com.fitombo.app.dto.objectif.ModifierObjectifRequest;
import com.fitombo.app.dto.objectif.ObjectifDto;
import com.fitombo.app.service.objectif.ObjectifService;
import jakarta.validation.Valid;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/objectifs")
@RequiredArgsConstructor
public class ObjectifController {
  private final ObjectifService objectifs;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ObjectifDto creer(
      @UtilisateurCourant UUID auteur,
      @RequestParam UUID exploitationId,
      @Valid @RequestBody CreerObjectifRequest demande) {
    return objectifs.creer(auteur, exploitationId, demande);
  }

  @GetMapping
  public PageResult<ObjectifDto> lister(
      @UtilisateurCourant UUID auteur,
      @RequestParam UUID exploitationId,
      @PageableDefault(size = 20, sort = "dateCible") Pageable page) {
    return objectifs.lister(
        auteur,
        exploitationId,
        Pagination.stable(page, "id", Set.of("id", "dateCible", "dateDebut")));
  }

  @GetMapping({"/{id}", "/{id}/progression"})
  public ObjectifDto consulter(@UtilisateurCourant UUID auteur, @PathVariable UUID id) {
    return objectifs.consulter(auteur, id);
  }

  @PutMapping("/{id}")
  public ObjectifDto modifier(
      @UtilisateurCourant UUID auteur,
      @PathVariable UUID id,
      @Valid @RequestBody ModifierObjectifRequest demande) {
    return objectifs.modifier(auteur, id, demande);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void abandonner(@UtilisateurCourant UUID auteur, @PathVariable UUID id) {
    objectifs.abandonner(auteur, id);
  }
}
