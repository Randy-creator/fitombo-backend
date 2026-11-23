package com.fitombo.app.service.historique;

import com.fitombo.app.dto.PageResult;
import com.fitombo.app.dto.historique.EvenementHistoriqueDto;
import com.fitombo.app.dto.historique.FiltresEvenements;
import com.fitombo.app.exception.CodeErreur;
import com.fitombo.app.exception.ExceptionApplicative;
import com.fitombo.app.repository.animal.AnimalRepository;
import com.fitombo.app.repository.historique.HistoriqueRepository;
import com.fitombo.app.repository.lot.LotRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
public class HistoriqueService {
  private final HistoriqueRepository historique;
  private final AnimalRepository animaux;
  private final LotRepository lots;

  public PageResult<EvenementHistoriqueDto> lister(
      UUID utilisateurId, FiltresEvenements filtres, Pageable pageable) {
    verifierCible(utilisateurId, filtres);
    return PageResult.from(historique.findHistorique(filtres, pageable));
  }

  private void verifierCible(UUID utilisateurId, FiltresEvenements filtres) {
    if (filtres.animalId() != null) {
      if (!animaux.existeVisible(filtres.animalId(), utilisateurId)) {
        throw new ExceptionApplicative(CodeErreur.ANIMAL_INTROUVABLE);
      }
    } else if (!lots.existeVisible(filtres.lotId(), utilisateurId)) {
      throw new ExceptionApplicative(CodeErreur.LOT_INTROUVABLE);
    }
  }
}
