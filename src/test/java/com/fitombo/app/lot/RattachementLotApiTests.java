package com.fitombo.app.lot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fitombo.app.ApiIntegrationTest;
import jakarta.persistence.EntityManager;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;

@Sql("/sql/exploitations-api.sql")
class RattachementLotApiTests extends ApiIntegrationTest {
  @Autowired private JdbcTemplate jdbc;
  @Autowired private EntityManager entityManager;
  private UUID porc;
  private UUID lot;

  @BeforeEach
  void preparerCheptel() {
    porc =
        jdbc.queryForObject(
            """
            insert into animal (exploitation_id, espece_code, identifiant, sexe)
            values ('30000000-0000-4000-8000-000000000001', 'PORC', 'PORC-INDIV', 'MALE') returning id
            """,
            UUID.class);
    lot = creerLot("LOT-A", "30000000-0000-4000-8000-000000000001");
  }

  @Test
  void rattacheEtRetireSansModifierLEffectifDuLot() throws Exception {
    rattacher(1, lot, porc).andExpect(status().isNoContent());
    rattacher(1, lot, porc).andExpect(status().isNoContent());
    entityManager.flush();
    assertThat(jdbc.queryForObject("select lot_id from animal where id = ?", UUID.class, porc))
        .isEqualTo(lot);
    assertThat(
            jdbc.queryForObject("select effectif_actuel from lot where id = ?", Integer.class, lot))
        .isEqualTo(3);
    for (int appel = 0; appel < 2; appel++) {
      mvc.perform(delete("/lots/{lotId}/animaux/{animalId}", lot, porc).with(utilisateur(1)))
          .andExpect(status().isNoContent());
    }
    entityManager.flush();
    assertThat(jdbc.queryForObject("select lot_id from animal where id = ?", UUID.class, porc))
        .isNull();
  }

  @Test
  void refuseLesRattachementsHorsPerimetreOuIncompatibles() throws Exception {
    rattacher(2, lot, porc).andExpect(status().isNotFound());
    UUID lotExterne = creerLot("LOT-PRIVE", "30000000-0000-4000-8000-000000000002");
    rattacher(1, lotExterne, porc).andExpect(status().isNotFound());

    UUID zebu =
        jdbc.queryForObject("select id from animal where identifiant = 'API-ACTIF'", UUID.class);
    rattacher(1, lot, zebu)
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("ESPECE_LOT_INCOMPATIBLE"));

    rattacher(1, lot, porc).andExpect(status().isNoContent());
    UUID autre = creerLot("LOT-B", "30000000-0000-4000-8000-000000000001");
    rattacher(1, autre, porc)
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("ANIMAL_DEJA_RATTACHE"));
  }

  private ResultActions rattacher(int utilisateur, UUID lotId, UUID animalId) throws Exception {
    return mvc.perform(
        post("/lots/{lotId}/animaux", lotId)
            .with(utilisateur(utilisateur))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"animalId\":\"%s\"}".formatted(animalId)));
  }

  private UUID creerLot(String code, String exploitationId) {
    return jdbc.queryForObject(
        """
        insert into lot (exploitation_id, espece_code, code, effectif_initial, effectif_actuel)
        values (?, 'PORC', ?, 3, 3) returning id
        """,
        UUID.class,
        UUID.fromString(exploitationId),
        code);
  }
}
