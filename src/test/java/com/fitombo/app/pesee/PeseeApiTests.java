package com.fitombo.app.pesee;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fitombo.app.ApiIntegrationTest;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;

@Sql("/sql/exploitations-api.sql")
class PeseeApiTests extends ApiIntegrationTest {
  @Autowired private JdbcTemplate jdbc;

  @Test
  void calculeEtPersisteUnePeseeAuRuban() throws Exception {
    UUID animal = animal("API-ACTIF");
    peser(
            1,
            """
            "animalId":"%s","methode":"RUBAN","perimetreThoraciqueCm":200,"longueurCm":150
            """
                .formatted(animal))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.poidsKg").value(480.0));
    assertThat(
            jdbc.queryForObject(
                """
                select p.poids_kg from pesee p join evenement e on e.id = p.evenement_id where e.animal_id = ?
                """,
                BigDecimal.class,
                animal))
        .isEqualByComparingTo("480.000");
  }

  @Test
  void valideLEchantillonDesLots() throws Exception {
    UUID lot =
        jdbc.queryForObject(
            """
            insert into lot (exploitation_id, espece_code, code, effectif_initial, effectif_actuel)
            values ('30000000-0000-4000-8000-000000000001','PORC','PESER-LOT',5,5) returning id
            """,
            UUID.class);
    String options =
        "\"lotId\":\"%s\",\"methode\":\"BALANCE\",\"poidsKg\":12,\"nbEchantillon\":".formatted(lot);
    peser(1, options + "6").andExpect(status().isBadRequest());
    peser(1, options + "2")
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.nbEchantillon").value(2));
  }

  @Test
  void refuseUneCibleSortieOuUneSaisieNonAutorisee() throws Exception {
    String options = "\"animalId\":\"%s\",\"methode\":\"BALANCE\",\"poidsKg\":100";
    peser(1, options.formatted(animal("API-VENDU"))).andExpect(status().isConflict());
    peser(2, options.formatted(animal("API-ACTIF"))).andExpect(status().isNotFound());
    peser(1, options.formatted(animal("API-PRIVE"))).andExpect(status().isNotFound());
    peser(1, "\"methode\":\"BALANCE\",\"poidsKg\":100").andExpect(status().isBadRequest());
  }

  private UUID animal(String identifiant) {
    return jdbc.queryForObject(
        "select id from animal where identifiant = ?", UUID.class, identifiant);
  }

  private ResultActions peser(int numero, String options) throws Exception {
    return mvc.perform(
        post("/evenements")
            .with(utilisateur(numero))
            .contentType(MediaType.APPLICATION_JSON)
            .content(
                """
                {"type":"PESEE","exploitationId":"30000000-0000-4000-8000-000000000001",
                 "dateEvenement":"2026-01-01",%s}
                """
                    .formatted(options)));
  }
}
