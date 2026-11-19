package com.fitombo.app.animal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fitombo.app.ApiIntegrationTest;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

@Sql("/sql/exploitations-api.sql")
class FiltresAnimauxApiTests extends ApiIntegrationTest {
  private static final String URL = "/exploitations/30000000-0000-4000-8000-000000000001/animaux";
  @Autowired private JdbcTemplate jdbc;

  @Test
  void combineLesFiltresSurLeCheptelAutorise() throws Exception {
    UUID emplacement =
        jdbc.queryForObject(
            """
            insert into emplacement (exploitation_id, nom, type)
            values ('30000000-0000-4000-8000-000000000001', 'Parc filtre', 'PARC') returning id
            """,
            UUID.class);
    jdbc.update(
        """
        update animal set date_naissance = current_date - 45, emplacement_id = ?, nom = 'Mina'
        where identifiant = 'API-ACTIF'
        """,
        emplacement);
    mvc.perform(
            get(URL)
                .with(utilisateur(2))
                .param("especeCode", "ZEBU")
                .param("emplacementId", emplacement.toString())
                .param("sexe", "MALE")
                .param("ageMinJours", "40")
                .param("ageMaxJours", "50")
                .param("recherche", "mina"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(1))
        .andExpect(jsonPath("$.items[0].identifiant").value("API-ACTIF"));
    mvc.perform(get(URL).with(utilisateur(2)).param("ageMinJours", "46"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(0));
  }

  @Test
  void rejetteUneTrancheAgeInversee() throws Exception {
    mvc.perform(get(URL).with(utilisateur(2)).param("ageMinJours", "50").param("ageMaxJours", "40"))
        .andExpect(status().isBadRequest());
  }
}
