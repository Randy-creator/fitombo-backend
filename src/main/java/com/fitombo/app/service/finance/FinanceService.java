package com.fitombo.app.service.finance;

import com.fitombo.app.dto.PageResult;
import com.fitombo.app.dto.finance.CreerTransactionRequest;
import com.fitombo.app.dto.finance.FiltreMarge;
import com.fitombo.app.dto.finance.MargeDto;
import com.fitombo.app.dto.finance.TransactionDto;
import com.fitombo.app.exception.CodeErreur;
import com.fitombo.app.exception.ExceptionApplicative;
import com.fitombo.app.repository.animal.AnimalRepository;
import com.fitombo.app.repository.finance.FinanceRepository;
import com.fitombo.app.repository.lot.LotRepository;
import com.fitombo.app.service.exploitation.ExploitationService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FinanceService {
  private final ExploitationService exploitations;
  private final FinanceRepository finances;
  private final AnimalRepository animaux;
  private final LotRepository lots;

  @Transactional
  public TransactionDto enregistrer(
      UUID utilisateur, UUID exploitation, CreerTransactionRequest demande) {
    exploitations.chargerPourGestion(utilisateur, exploitation);
    verifierCible(exploitation, demande.animalId(), demande.lotId());
    return finances.enregistrer(exploitation, utilisateur, demande);
  }

  @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
  public PageResult<TransactionDto> lister(UUID utilisateur, UUID exploitation, Pageable page) {
    exploitations.chargerPourGestion(utilisateur, exploitation);
    return PageResult.from(finances.lister(exploitation, page));
  }

  @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
  public MargeDto marge(UUID utilisateur, UUID exploitation, FiltreMarge filtre) {
    exploitations.chargerPourGestion(utilisateur, exploitation);
    verifierCible(exploitation, filtre.animalId(), filtre.lotId());
    return finances.marge(exploitation, filtre);
  }

  private void verifierCible(UUID exploitation, UUID animal, UUID lot) {
    if (animal != null
        && !animaux.existsByIdAndExploitationIdAndDeletedAtIsNull(animal, exploitation)) {
      throw new ExceptionApplicative(CodeErreur.ANIMAL_INTROUVABLE);
    }
    if (lot != null && !lots.existsByIdAndExploitationIdAndDeletedAtIsNull(lot, exploitation)) {
      throw new ExceptionApplicative(CodeErreur.LOT_INTROUVABLE);
    }
  }
}
