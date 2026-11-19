package com.fitombo.app.service.deces;

import com.fitombo.app.dto.deces.CreerDecesRequest;
import com.fitombo.app.model.cheptel.StatutAnimal;
import com.fitombo.app.model.cheptel.StatutLot;
import com.fitombo.app.model.evenement.Deces;
import com.fitombo.app.service.cheptel.SortieCheptel;
import com.fitombo.app.service.evenement.CiblesEvenement.Cible;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DecesService {
  private final SortieCheptel sorties;

  public Deces creer(Cible cible, CreerDecesRequest demande) {
    StatutAnimal statut =
        switch (demande.cause()) {
          case VOL -> StatutAnimal.VOLE;
          case PERTE -> StatutAnimal.PERDU;
          default -> StatutAnimal.MORT;
        };
    StatutLot statutLot = statut == StatutAnimal.MORT ? StatutLot.TERMINE : StatutLot.PERDU;
    sorties.appliquer(cible, demande.nombre(), demande.dateEvenement(), statut, statutLot);
    return new Deces(
        cible.exploitation(),
        cible.animal(),
        cible.lot(),
        demande.dateEvenement(),
        cible.auteur(),
        demande.notes(),
        demande.cause(),
        demande.nombre(),
        demande.maladieSuspectee(),
        Boolean.TRUE.equals(demande.declarationFaite()));
  }
}
