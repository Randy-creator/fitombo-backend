package com.fitombo.app.service;

import com.fitombo.app.dto.EspeceDto;
import com.fitombo.app.dto.PageResult;
import com.fitombo.app.dto.RaceDto;
import com.fitombo.app.exception.CodeErreur;
import com.fitombo.app.exception.ExceptionApplicative;
import com.fitombo.app.repository.EspeceRepository;
import com.fitombo.app.repository.RaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReferentielService {

  private final EspeceRepository especes;
  private final RaceRepository races;

  public PageResult<EspeceDto> listerEspeces(Pageable pageable) {
    return PageResult.from(especes.findActives(pageable));
  }

  public PageResult<RaceDto> listerRaces(String code, Pageable pageable) {
    if (!especes.existsByCodeAndActifTrue(code)) {
      throw new ExceptionApplicative(CodeErreur.ESPECE_INTROUVABLE);
    }
    return PageResult.from(races.findActivesParEspece(code, pageable));
  }
}
