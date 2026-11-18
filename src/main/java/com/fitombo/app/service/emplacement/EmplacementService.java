package com.fitombo.app.service.emplacement;

import com.fitombo.app.dto.PageResult;
import com.fitombo.app.dto.emplacement.EmplacementDto;
import com.fitombo.app.repository.emplacement.EmplacementRepository;
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
public class EmplacementService {
  private final ExploitationService exploitations;
  private final EmplacementRepository emplacements;

  public PageResult<EmplacementDto> lister(
      UUID utilisateurId, UUID exploitationId, Pageable pageable) {
    exploitations.consulter(utilisateurId, exploitationId);
    return PageResult.from(emplacements.findVisibles(exploitationId, pageable));
  }
}
