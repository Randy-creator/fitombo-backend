package com.fitombo.app.service.synchronisation;

import com.fitombo.app.dto.synchronisation.LotSynchronisationRequest;
import com.fitombo.app.dto.synchronisation.ResultatSynchronisation;
import com.fitombo.app.dto.synchronisation.ResultatSynchronisation.Statut;
import com.fitombo.app.exception.ExceptionApplicative;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SynchronisationService {
  private final ReceptionSyncService reception;

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
