package com.fitombo.app.service.referentiel;

import com.fitombo.app.dto.PageResult;
import com.fitombo.app.dto.referentiel.EspeceDto;
import com.fitombo.app.dto.referentiel.EtapeSanitaireDto;
import com.fitombo.app.dto.referentiel.RaceDto;
import com.fitombo.app.exception.CodeErreur;
import com.fitombo.app.exception.ExceptionApplicative;
import com.fitombo.app.repository.referentiel.EspeceRepository;
import com.fitombo.app.repository.referentiel.ProtocoleRepository;
import com.fitombo.app.repository.referentiel.RaceRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReferentielService {

  private final EspeceRepository especes;
  private final ProtocoleRepository protocoles;
  private final RaceRepository races;

  public PageResult<EspeceDto> listerEspeces(Pageable pageable) {
    return PageResult.from(especes.findActives(pageable));
  }

  public PageResult<RaceDto> listerRaces(String code, Pageable pageable) {
    verifierEspece(code);
    return PageResult.from(races.findActivesParEspece(code, pageable));
  }

  public List<EtapeSanitaireDto> listerProtocoles(String code) {
    verifierEspece(code);
    return protocoles.etapes(code);
  }

  private void verifierEspece(String code) {
    if (!especes.existsByCodeAndActifTrue(code)) {
      throw new ExceptionApplicative(CodeErreur.ESPECE_INTROUVABLE);
    }
  }
}
