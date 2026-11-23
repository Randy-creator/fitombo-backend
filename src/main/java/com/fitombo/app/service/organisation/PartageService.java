package com.fitombo.app.service.organisation;

import com.fitombo.app.dto.organisation.PartageDto;
import com.fitombo.app.dto.organisation.StatistiquesOrganisationDto;
import com.fitombo.app.exception.CodeErreur;
import com.fitombo.app.exception.ExceptionApplicative;
import com.fitombo.app.repository.organisation.PartageRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PartageService {
  private final PartageRepository partage;

  @Value("${app.organisation.minimum-participants:3}")
  private int minimumParticipants;

  @Transactional(readOnly = true)
  public PartageDto consulter(UUID utilisateur) {
    return partage
        .consulter(utilisateur)
        .orElseThrow(() -> new ExceptionApplicative(CodeErreur.ELEVEUR_INTROUVABLE));
  }

  @Transactional
  public PartageDto modifier(UUID utilisateur, boolean autorise, UUID organisation) {
    consulter(utilisateur);
    if (partage.modifier(utilisateur, autorise, organisation) == 0) {
      throw new ExceptionApplicative(CodeErreur.PARTAGE_INDISPONIBLE);
    }
    return consulter(utilisateur);
  }

  @Transactional(readOnly = true)
  public StatistiquesOrganisationDto statistiques(UUID utilisateur) {
    return partage
        .statistiques(utilisateur, Math.max(3, minimumParticipants))
        .orElseThrow(() -> new ExceptionApplicative(CodeErreur.PARTAGE_INDISPONIBLE));
  }
}
