package com.fitombo.app.controller.evenement;

import com.fitombo.app.controller.Pagination;
import com.fitombo.app.controller.UtilisateurCourant;
import com.fitombo.app.dto.PageResult;
import com.fitombo.app.dto.evenement.AnnulerEvenementRequest;
import com.fitombo.app.dto.evenement.CorrigerEvenementRequest;
import com.fitombo.app.dto.evenement.SaisieEvenement;
import com.fitombo.app.dto.historique.EvenementHistoriqueDto;
import com.fitombo.app.dto.historique.FiltresEvenements;
import com.fitombo.app.dto.pesee.CreerPeseeRequest;
import com.fitombo.app.service.evenement.RevisionEvenementService;
import com.fitombo.app.service.evenement.SaisieEvenementService;
import com.fitombo.app.service.historique.HistoriqueService;
import com.fitombo.app.service.pesee.PeseeService;
import jakarta.validation.Valid;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class EvenementController {
  private final HistoriqueService historique;
  private final PeseeService pesees;
  private final SaisieEvenementService saisies;
  private final RevisionEvenementService revisions;

  @GetMapping("/evenements")
  public PageResult<EvenementHistoriqueDto> lister(
      @UtilisateurCourant UUID utilisateurId,
      @Valid @ModelAttribute FiltresEvenements filtres,
      @PageableDefault(size = 20, sort = "dateEvenement", direction = Sort.Direction.DESC)
          Pageable pageable) {
    return historique.lister(utilisateurId, filtres, stabiliser(pageable));
  }

  @GetMapping("/animaux/{id}/historique")
  public PageResult<EvenementHistoriqueDto> historiqueAnimal(
      @UtilisateurCourant UUID utilisateurId,
      @PathVariable UUID id,
      @PageableDefault(size = 20, sort = "dateEvenement", direction = Sort.Direction.DESC)
          Pageable pageable) {
    return historique.lister(utilisateurId, FiltresEvenements.pourAnimal(id), stabiliser(pageable));
  }

  @PostMapping("/evenements")
  @ResponseStatus(HttpStatus.CREATED)
  public Object enregistrer(
      @UtilisateurCourant UUID utilisateurId, @Valid @RequestBody SaisieEvenement demande) {
    if (demande instanceof CreerPeseeRequest pesee) {
      return pesees.enregistrer(utilisateurId, pesee);
    }
    return saisies.enregistrer(utilisateurId, demande);
  }

  @PutMapping("/evenements/{id}")
  @ResponseStatus(HttpStatus.CREATED)
  public Object corriger(
      @UtilisateurCourant UUID utilisateurId,
      @PathVariable UUID id,
      @Valid @RequestBody CorrigerEvenementRequest demande) {
    return revisions.corriger(utilisateurId, id, demande.motif(), demande.saisie());
  }

  @PatchMapping("/evenements/{id}/annulation")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void annuler(
      @UtilisateurCourant UUID utilisateurId,
      @PathVariable UUID id,
      @Valid @RequestBody AnnulerEvenementRequest demande) {
    revisions.annuler(utilisateurId, id, demande.motif());
  }

  private Pageable stabiliser(Pageable pageable) {
    return Pagination.stable(pageable, "id", Set.of("dateEvenement", "id"));
  }
}
