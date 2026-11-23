package com.fitombo.app.historique;

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
class HistoriqueApiTests extends ApiIntegrationTest {
  @Autowired private JdbcTemplate jdbc;

  @Test
  void restitueLesEvenementsEtLeurEtatAvecLesFiltres() throws Exception {
    UUID animal = animal("API-ACTIF");
    inserer(animal, "2026-01-02", "ANNULE");
    inserer(animal, "2026-01-03", "VALIDE");
    mvc.perform(get("/animaux/{id}/historique", animal).with(utilisateur(2)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(2))
        .andExpect(jsonPath("$.items[0].type").value("PESEE"))
        .andExpect(jsonPath("$.items[0].statut").value("VALIDE"));
    mvc.perform(
            get("/evenements")
                .with(utilisateur(2))
                .param("animalId", animal.toString())
                .param("type", "PESEE")
                .param("au", "2026-01-02"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(1))
        .andExpect(jsonPath("$.items[0].statut").value("ANNULE"));
  }

  @Test
  void protegeLHistoriqueEtRejetteLesFiltresIncoherents() throws Exception {
    UUID prive = animal("API-PRIVE");
    mvc.perform(get("/animaux/{id}/historique", prive).with(utilisateur(2)))
        .andExpect(status().isNotFound());
    mvc.perform(get("/evenements").with(utilisateur(2))).andExpect(status().isBadRequest());
    mvc.perform(
            get("/evenements")
                .with(utilisateur(2))
                .param("animalId", prive.toString())
                .param("du", "2026-02-01")
                .param("au", "2026-01-01"))
        .andExpect(status().isBadRequest());
  }

  private UUID animal(String identifiant) {
    return jdbc.queryForObject(
        "select id from animal where identifiant = ?", UUID.class, identifiant);
  }

  private void inserer(UUID animal, String date, String statut) {
    jdbc.update(
        """
        with insertion as (
          insert into evenement (exploitation_id, type, animal_id, date_evenement, saisi_par, statut)
          values ('30000000-0000-4000-8000-000000000001', 'PESEE', ?, cast(? as date),
                  '10000000-0000-4000-8000-000000000001', ?) returning id
        )
        insert into pesee (evenement_id, poids_kg) select id, 100 from insertion
        """,
        animal,
        date,
        statut);
  }
}
