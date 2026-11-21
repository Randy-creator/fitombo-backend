package com.fitombo.app.alerte;

import static org.assertj.core.api.Assertions.assertThat;

import com.fitombo.app.ApiIntegrationTest;
import com.fitombo.app.service.alerte.GenerationAlertesService;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

@Sql("/sql/exploitations-api.sql")
class GenerationAlertesTests extends ApiIntegrationTest {
  @Autowired private JdbcTemplate jdbc;
  @Autowired private GenerationAlertesService generation;

  @Test
  void programmeUnSeulRappelPourUnEvenementValide() {
    UUID evenement =
        jdbc.queryForObject(
            """
            insert into evenement(exploitation_id, animal_id, type, date_evenement, saisi_par)
            select exploitation_id, id, 'VACCINATION', '2026-06-01',
              '10000000-0000-4000-8000-000000000001' from animal where identifiant = 'API-ACTIF'
            returning id
            """,
            UUID.class);
    jdbc.update(
        "insert into vaccination(evenement_id, vaccin, rappel_prevu) values (?, 'Test', '2026-06-07')",
        evenement);
    LocalDate jour = LocalDate.of(2026, 6, 1);
    assertThat(generation.generer(jour)).isEqualTo(1);
    assertThat(generation.generer(jour)).isZero();
    assertThat(
            jdbc.queryForObject(
                "select type from alerte where evenement_id = ?", String.class, evenement))
        .isEqualTo("RAPPEL_VACCIN");
  }
}
