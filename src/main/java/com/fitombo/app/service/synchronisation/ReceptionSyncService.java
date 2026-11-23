package com.fitombo.app.service.synchronisation;

import com.fitombo.app.dto.pesee.CreerPeseeRequest;
import com.fitombo.app.dto.synchronisation.LotSynchronisationRequest.Operation;
import com.fitombo.app.dto.synchronisation.ResultatSynchronisation;
import com.fitombo.app.dto.synchronisation.ResultatSynchronisation.Statut;
import com.fitombo.app.exception.CodeErreur;
import com.fitombo.app.exception.ExceptionApplicative;
import com.fitombo.app.repository.synchronisation.ReceptionSyncRepository;
import com.fitombo.app.service.evenement.SaisieEvenementService;
import com.fitombo.app.service.exploitation.ExploitationService;
import com.fitombo.app.service.pesee.PeseeService;
import jakarta.validation.Validator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class ReceptionSyncService {
  private final ReceptionSyncRepository receptions;
  private final PeseeService pesees;
  private final SaisieEvenementService saisies;
  private final ObjectMapper json;
  private final ExploitationService exploitations;
  private final Validator validation;

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public ResultatSynchronisation traiter(UUID utilisateur, Operation operation) {
    if (!validation.validate(operation.saisie()).isEmpty()) {
      throw new ExceptionApplicative(CodeErreur.OPERATION_SYNC_INVALIDE);
    }
    exploitations.chargerPourGestion(utilisateur, operation.saisie().exploitationId());
    UUID reference = operation.operationId();
    String empreinte = empreinte(operation);
    if (!receptions.reserver(utilisateur, reference, empreinte)) {
      var precedente = receptions.retrouver(utilisateur, reference);
      if (!empreinte.equals(precedente.empreinte())) {
        throw new ExceptionApplicative(CodeErreur.OPERATION_SYNC_REUTILISEE);
      }
      return new ResultatSynchronisation(reference, Statut.DOUBLON, precedente.evenementId(), null);
    }

    UUID evenement =
        operation.saisie() instanceof CreerPeseeRequest pesee
            ? pesees.enregistrer(utilisateur, pesee).id()
            : saisies.enregistrer(utilisateur, operation.saisie()).id();
    receptions.terminer(utilisateur, reference, evenement);
    return new ResultatSynchronisation(reference, Statut.ACCEPTE, evenement, null);
  }

  private String empreinte(Operation operation) {
    try {
      byte[] contenu = json.writeValueAsBytes(operation.saisie());
      return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(contenu));
    } catch (NoSuchAlgorithmException erreur) {
      throw new IllegalStateException("SHA-256 indisponible", erreur);
    }
  }
}
