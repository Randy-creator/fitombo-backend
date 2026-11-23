package com.fitombo.app.croissance;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fitombo.app.ApiIntegrationTest;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

@Sql("/sql/exploitations-api.sql")
class CroissanceApiTests extends ApiIntegrationTest {
  private static final String EXPLOITATION = "30000000-0000-4000-8000-000000000001";
  @Autowired private JdbcTemplate jdbc;

  private UUID animal() {
    return jdbc.queryForObject("select id from animal where identifiant = 'API-ACTIF'", UUID.class);
  }

  private void peser(UUID animal, String date, int poids) throws Exception {
    mvc.perform(
            post("/evenements")
                .with(utilisateur(1))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"type":"PESEE","exploitationId":"%s","animalId":"%s",
                     "dateEvenement":"%s","methode":"BALANCE","poidsKg":%d}
                    """
                        .formatted(EXPLOITATION, animal, date, poids)))
        .andExpect(status().isCreated());
  }

  @Test
  void compareLesPeseesALaCourbeDeRaceEtIgnoreLesAnnulations() throws Exception {
    UUID animal = animal();
    jdbc.update(
        """
        update animal set date_naissance = '2026-01-01',
          race_id = (select c.race_id from courbe_reference c join race r on c.race_id = r.id
                     where r.espece_code = 'ZEBU' and c.sexe = 'MALE' limit 1)
        where id = ?
        """,
        animal);
    peser(animal, "2026-06-01", 180);
    peser(animal, "2026-06-11", 190);
    peser(animal, "2026-06-21", 200);
    jdbc.update("update evenement set statut = 'ANNULE' where date_evenement = '2026-06-21'");
    mvc.perform(get("/animaux/{id}/croissance", animal).with(utilisateur(4)))
        .andExpect(status().isNotFound());
    mvc.perform(get("/animaux/{id}/croissance", animal).with(utilisateur(2)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.pesees.length()").value(2))
        .andExpect(jsonPath("$.gainMoyenQuotidienKg").value(1))
        .andExpect(jsonPath("$.reference.points[0].ageJours").exists())
        .andExpect(jsonPath("$.position").exists());
  }
}
