package com.fitombo.app.service.pesee;

import com.fitombo.app.dto.pesee.CreerPeseeRequest;
import com.fitombo.app.dto.pesee.PeseeDto;
import com.fitombo.app.model.cheptel.Animal;
import com.fitombo.app.model.cheptel.Lot;
import com.fitombo.app.model.evenement.Evenement;
import com.fitombo.app.model.evenement.Pesee;
import com.fitombo.app.repository.pesee.PeseeRepository;
import com.fitombo.app.service.evenement.CiblesEvenement;
import com.fitombo.app.service.evenement.CiblesEvenement.Cible;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PeseeService {
  private final CiblesEvenement cibles;
  private final PeseeRepository pesees;

  @Transactional
  public PeseeDto enregistrer(UUID utilisateurId, CreerPeseeRequest demande) {
    return enregistrer(cibles.charger(utilisateurId, demande), demande, null);
  }

  public PeseeDto enregistrer(Cible cible, CreerPeseeRequest demande, Evenement original) {
    Animal animal = cible.animal();
    Lot lot = cible.lot();
    String espece = animal == null ? lot.getEspece().getCode() : animal.getEspece().getCode();
    var poids = MesurePesee.calculer(demande, espece, lot == null ? null : lot.getEffectifActuel());
    var pesee =
        new Pesee(
            cible.exploitation(),
            animal,
            lot,
            demande.dateEvenement(),
            cible.auteur(),
            demande.notes(),
            demande.methode(),
            poids);
    pesee.renseignerMesures(
        demande.perimetreThoraciqueCm(), demande.longueurCm(), demande.nbEchantillon());
    if (original != null) {
      pesee.remplacer(original);
    }
    pesees.saveAndFlush(pesee);
    return new PeseeDto(
        pesee.getId(),
        demande.animalId(),
        demande.lotId(),
        pesee.getDateEvenement(),
        poids,
        pesee.getMethode(),
        pesee.getPerimetreThoraciqueCm(),
        pesee.getLongueurCm(),
        pesee.getNbEchantillon());
  }
}
