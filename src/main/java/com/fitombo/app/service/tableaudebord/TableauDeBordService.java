package com.fitombo.app.service.tableaudebord;

import com.fitombo.app.dto.finance.FiltreMarge;
import com.fitombo.app.dto.tableaudebord.TableauDeBordDto;
import com.fitombo.app.repository.finance.FinanceRepository;
import com.fitombo.app.repository.tableaudebord.TableauDeBordRepository;
import com.fitombo.app.service.exploitation.ExploitationService;
import com.fitombo.app.service.objectif.ObjectifService;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TableauDeBordService {
  private final ExploitationService exploitations;
  private final ObjectifService objectifs;
  private final TableauDeBordRepository tableaux;
  private final FinanceRepository finances;

  @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
  public TableauDeBordDto consulter(UUID auteur, UUID exploitation, LocalDate du, LocalDate au) {
    exploitations.chargerPourGestion(auteur, exploitation);
    LocalDate jour = LocalDate.now();
    return new TableauDeBordDto(
        du,
        au,
        tableaux.effectifs(exploitation),
        tableaux.alertesDuJour(exploitation, auteur, jour),
        objectifs.compterEnCours(auteur, exploitation, jour),
        tableaux.deces(exploitation, du, au),
        finances.marge(exploitation, new FiltreMarge(du, au, null, null)));
  }
}
