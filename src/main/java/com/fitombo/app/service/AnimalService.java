package com.fitombo.app.service;

import com.fitombo.app.dto.AnimalDto;
import com.fitombo.app.dto.PageResult;
import com.fitombo.app.dto.animal.FiltresAnimaux;
import com.fitombo.app.exception.CodeErreur;
import com.fitombo.app.exception.ExceptionApplicative;
import com.fitombo.app.repository.AnimalRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
public class AnimalService {

  private final ExploitationService exploitations;
  private final AnimalRepository animaux;

  public PageResult<AnimalDto> lister(
      UUID utilisateurId, UUID exploitationId, FiltresAnimaux filtres, Pageable pageable) {
    exploitations.verifierAcces(utilisateurId, exploitationId);
    return PageResult.from(animaux.findParExploitation(exploitationId, filtres, pageable));
  }

  public AnimalDto consulter(UUID utilisateurId, UUID id) {
    return animaux
        .findVisibleParId(id, utilisateurId)
        .orElseThrow(() -> new ExceptionApplicative(CodeErreur.ANIMAL_INTROUVABLE));
  }
}
