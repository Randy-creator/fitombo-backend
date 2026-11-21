package com.fitombo.app.service.alerte;

import com.fitombo.app.repository.alerte.GenerationAlertesRepository;
import java.time.LocalDate;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GenerationAlertesService {
  private final GenerationAlertesRepository alertes;

  @Scheduled(cron = "${app.alertes.cron:0 0 6 * * *}", zone = "Indian/Antananarivo")
  @Transactional
  public void genererQuotidiennement() {
    alertes.generer(LocalDate.now(ZoneId.of("Indian/Antananarivo")));
  }

  @Transactional
  public int generer(LocalDate jour) {
    return alertes.generer(jour);
  }
}
