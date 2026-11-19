package com.fitombo.app.controller.referentiel;

import com.fitombo.app.controller.Pagination;
import com.fitombo.app.dto.PageResult;
import com.fitombo.app.dto.referentiel.EspeceDto;
import com.fitombo.app.dto.referentiel.RaceDto;
import com.fitombo.app.service.referentiel.ReferentielService;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/especes")
@RequiredArgsConstructor
public class ReferentielController {

  private final ReferentielService referentiel;

  @GetMapping
  public PageResult<EspeceDto> especes(
      @PageableDefault(size = 20, sort = "nomFr") Pageable pageable) {
    Pageable pagination = Pagination.stable(pageable, "code", Set.of("nomFr", "code"));
    return referentiel.listerEspeces(pagination);
  }

  @GetMapping("/{code}/races")
  public PageResult<RaceDto> races(
      @PathVariable String code, @PageableDefault(size = 20, sort = "nom") Pageable pageable) {
    Pageable pagination = Pagination.stable(pageable, "id", Set.of("nom", "id"));
    return referentiel.listerRaces(code, pagination);
  }
}
