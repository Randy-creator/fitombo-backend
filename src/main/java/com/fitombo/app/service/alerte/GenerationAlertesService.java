package com.fitombo.app.service.alerte;

import com.fitombo.app.repository.alerte.GenerationAlertesRepository;
import com.fitombo.app.repository.objectif.ObjectifRepository;
import com.fitombo.app.service.objectif.ObjectifService;
import java.time.LocalDate;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GenerationAlertesService {
  private final GenerationAlertesRepository alertes;
  private final ObjectifRepository objectifs;
  private final ObjectifService progression;

  @Scheduled(cron = "${app.alertes.cron:0 0 6 * * *}", zone = "Indian/Antananarivo")
  @Transactional
  public void genererQuotidiennement() {
    generer(LocalDate.now(ZoneId.of("Indian/Antananarivo")));
  }

  @Transactional
  public int generer(LocalDate jour) {
    return alertes.generer(jour) + signalerObjectifsEnRetard(jour);
  }

  private int signalerObjectifsEnRetard(LocalDate jour) {
    int nouvellesAlertes = 0;
    Pageable page = PageRequest.of(0, 100);
    while (page.isPaged()) {
      var objectifsEchus = objectifs.objectifsEchus(jour, page);
      for (var objectif : objectifsEchus) {
        if (progression.estEnEchec(objectif, jour)) {
          nouvellesAlertes += alertes.signalerObjectifEnRetard(objectif.getId());
        }
      }
      page = objectifsEchus.nextPageable();
    }
    return nouvellesAlertes;
  }
}
