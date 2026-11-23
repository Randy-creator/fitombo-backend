package com.fitombo.app.service.synchronisation;

import com.fitombo.app.dto.synchronisation.FenetreSynchronisation;
import com.fitombo.app.dto.synchronisation.LotSynchronisationRequest;
import com.fitombo.app.dto.synchronisation.PageSynchronisation;
import com.fitombo.app.dto.synchronisation.ResultatSynchronisation;
import com.fitombo.app.dto.synchronisation.ResultatSynchronisation.Statut;
import com.fitombo.app.exception.ExceptionApplicative;
import com.fitombo.app.repository.synchronisation.JournalSyncRepository;
import com.fitombo.app.service.exploitation.ExploitationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SynchronisationService {
  private static final int TAILLE_PAGE = 200;
  private final ReceptionSyncService reception;
  private final JournalSyncRepository journal;
  private final ExploitationService exploitations;

  @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
  public PageSynchronisation recevoir(
      UUID utilisateur, UUID exploitation, FenetreSynchronisation curseur) {
    exploitations.verifierAcces(utilisateur, exploitation);
    var resultats = journal.changements(exploitation, curseur, TAILLE_PAGE + 1);
    boolean suite = resultats.size() > TAILLE_PAGE;
    var page = suite ? List.copyOf(resultats.subList(0, TAILLE_PAGE)) : resultats;
    if (page.isEmpty()) {
      return new PageSynchronisation(page, curseur, false);
    }
    var dernier = page.getLast();
    return new PageSynchronisation(
        page, new FenetreSynchronisation(dernier.transaction(), dernier.sequence()), suite);
  }

  public List<ResultatSynchronisation> envoyer(
      UUID utilisateur, LotSynchronisationRequest demande) {
    return demande.operations().stream().map(operation -> traiter(utilisateur, operation)).toList();
  }

  private ResultatSynchronisation traiter(
      UUID utilisateur, LotSynchronisationRequest.Operation operation) {
    try {
      return reception.traiter(utilisateur, operation);
    } catch (ExceptionApplicative erreur) {
      return new ResultatSynchronisation(
          operation.operationId(), Statut.REJETE, null, erreur.code());
    }
  }
}
