package com.fitombo.app.service.cheptel;

import com.fitombo.app.exception.CodeErreur;
import com.fitombo.app.exception.ExceptionApplicative;
import com.fitombo.app.model.cheptel.StatutAnimal;
import com.fitombo.app.model.cheptel.StatutLot;
import com.fitombo.app.service.evenement.CiblesEvenement.Cible;
import java.time.LocalDate;
import org.springframework.stereotype.Component;

@Component
public class SortieCheptel {
  public void appliquer(
      Cible cible, int nombre, LocalDate date, StatutAnimal statutAnimal, StatutLot statutLot) {
    if (cible.animal() != null) {
      var animal = cible.animal();
      verifier(
          nombre == 1
              && (animal.getDateNaissance() == null || !date.isBefore(animal.getDateNaissance())));
      animal.enregistrerSortie(statutAnimal, date);
      return;
    }

    var lot = cible.lot();
    verifier(nombre <= lot.getEffectifActuel() && !date.isBefore(lot.getDateCreation()));
    lot.retirerEffectif(nombre, statutLot, date);
  }

  private void verifier(boolean valide) {
    if (!valide) {
      throw new ExceptionApplicative(CodeErreur.SORTIE_CHEPTEL_INVALIDE);
    }
  }
}
