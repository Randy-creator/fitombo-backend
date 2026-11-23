package com.fitombo.app.service.alerte;

import com.fitombo.app.dto.PageResult;
import com.fitombo.app.dto.alerte.AlerteDto;
import com.fitombo.app.exception.CodeErreur;
import com.fitombo.app.exception.ExceptionApplicative;
import com.fitombo.app.model.alerte.Alerte;
import com.fitombo.app.model.alerte.StatutAlerte;
import com.fitombo.app.repository.alerte.AlerteRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AlerteService {
  private final AlerteRepository alertes;

  @Transactional(readOnly = true)
  public PageResult<AlerteDto> lister(UUID utilisateurId, StatutAlerte statut, Pageable pageable) {
    return PageResult.from(alertes.findVisibles(utilisateurId, statut, pageable));
  }

  @Transactional
  public void marquerVue(UUID utilisateurId, UUID id) {
    Alerte alerte =
        alertes
            .findAModifier(id, utilisateurId)
            .orElseThrow(() -> new ExceptionApplicative(CodeErreur.ALERTE_INTROUVABLE));
    alerte.marquerVue();
  }
}
