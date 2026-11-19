package com.fitombo.app.service.cheptel;

import com.fitombo.app.exception.CodeErreur;
import com.fitombo.app.exception.ExceptionApplicative;
import com.fitombo.app.model.exploitation.Emplacement;
import com.fitombo.app.model.referentiel.Espece;
import com.fitombo.app.model.referentiel.Race;
import com.fitombo.app.repository.emplacement.EmplacementRepository;
import com.fitombo.app.repository.referentiel.EspeceRepository;
import com.fitombo.app.repository.referentiel.RaceRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReferencesCheptel {
  private final EspeceRepository especes;
  private final RaceRepository races;
  private final EmplacementRepository emplacements;

  public Espece especeActive(String code) {
    return especes
        .findByCodeAndActifTrue(code)
        .orElseThrow(() -> new ExceptionApplicative(CodeErreur.ESPECE_INTROUVABLE));
  }

  public Race raceFacultative(UUID id, String especeCode) {
    if (id == null) {
      return null;
    }
    return races
        .findByIdAndEspeceCodeAndActifTrue(id, especeCode)
        .orElseThrow(() -> new ExceptionApplicative(CodeErreur.RACE_INCOMPATIBLE));
  }

  public Emplacement emplacementFacultatif(UUID id, UUID exploitationId) {
    if (id == null) {
      return null;
    }
    return emplacements
        .findByIdAndExploitationIdAndDeletedAtIsNull(id, exploitationId)
        .orElseThrow(() -> new ExceptionApplicative(CodeErreur.EMPLACEMENT_INTROUVABLE));
  }
}
