package com.fitombo.app.controller.tableaudebord;

import com.fitombo.app.controller.UtilisateurCourant;
import com.fitombo.app.dto.tableaudebord.PeriodeTableauDeBord;
import com.fitombo.app.dto.tableaudebord.TableauDeBordDto;
import com.fitombo.app.service.tableaudebord.TableauDeBordService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TableauDeBordController {
  private final TableauDeBordService tableaux;

  @GetMapping("/exploitations/{id}/tableau-de-bord")
  public TableauDeBordDto consulter(
      @UtilisateurCourant UUID utilisateurId,
      @PathVariable UUID id,
      @Valid @ModelAttribute PeriodeTableauDeBord periode) {
    return tableaux.consulter(utilisateurId, id, periode.du(), periode.au());
  }
}
