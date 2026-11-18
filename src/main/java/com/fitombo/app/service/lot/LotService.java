package com.fitombo.app.service.lot;

import com.fitombo.app.dto.PageResult;
import com.fitombo.app.dto.lot.LotDto;
import com.fitombo.app.model.cheptel.StatutLot;
import com.fitombo.app.repository.lot.LotRepository;
import com.fitombo.app.service.ExploitationService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
public class LotService {
  private final ExploitationService exploitations;
  private final LotRepository lots;

  public PageResult<LotDto> lister(
      UUID utilisateurId, UUID exploitationId, StatutLot statut, Pageable pageable) {
    exploitations.verifierAcces(utilisateurId, exploitationId);
    return PageResult.from(lots.findVisibles(exploitationId, statut, pageable));
  }
}
