package com.fitombo.app.lot;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fitombo.app.ApiIntegrationTest;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

@Sql(
    scripts = "/sql/exploitations-api.sql",
    statements =
        """
        insert into lot (exploitation_id, espece_code, code, effectif_initial, effectif_actuel, statut, date_cloture, deleted_at) values
        ('30000000-0000-4000-8000-000000000001', 'PORC', 'LOT-ACTIF', 20, 18, 'ACTIF', null, null),
        ('30000000-0000-4000-8000-000000000001', 'PORC', 'LOT-VENDU', 5, 0, 'VENDU', current_date, null),
        ('30000000-0000-4000-8000-000000000001', 'PORC', 'LOT-SUPPRIME', 1, 1, 'ACTIF', null, now());
        """)
class LotApiTests extends ApiIntegrationTest {
  private static final String URI = "/exploitations/30000000-0000-4000-8000-000000000001/lots";

  @Autowired private JdbcTemplate jdbc;

  @Test
  void consulteLaFicheEtLHistoriqueDansLePerimetreDuMembre() throws Exception {
    UUID id = jdbc.queryForObject("select id from lot where code = 'LOT-ACTIF'", UUID.class);
    mvc.perform(get("/lots/{id}", id).with(utilisateur(2)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.effectifActuel").value(18));
    mvc.perform(get("/lots/{id}/historique", id).with(utilisateur(2)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(0));
    mvc.perform(get("/lots/{id}", id).with(utilisateur(4))).andExpect(status().isNotFound());
    mvc.perform(get("/lots/{id}/historique", id).with(utilisateur(4)))
        .andExpect(status().isNotFound());
  }

  @Test
  void listeLesElementsVisiblesPourUnMembreActif() throws Exception {
    mvc.perform(get(URI).with(utilisateur(2)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(2))
        .andExpect(jsonPath("$.items[0].code").value("LOT-ACTIF"));

    mvc.perform(get(URI).param("statut", "VENDU").with(utilisateur(2)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(1))
        .andExpect(jsonPath("$.items[0].code").value("LOT-VENDU"));
  }

  @Test
  void masqueLesElementsAuxNonMembres() throws Exception {
    mvc.perform(get(URI).with(utilisateur(4))).andExpect(status().isNotFound());
  }
}
