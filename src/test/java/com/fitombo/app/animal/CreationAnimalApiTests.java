package com.fitombo.app.animal;

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
import org.springframework.test.web.servlet.ResultActions;

@Sql("/sql/exploitations-api.sql")
class CreationAnimalApiTests extends ApiIntegrationTest {
  private static final String URL = "/exploitations/30000000-0000-4000-8000-000000000001/animaux";
  @Autowired private JdbcTemplate jdbc;

  @Test
  void persisteUnAnimalActifEtSaFiliation() throws Exception {
    UUID pere = parent("API-ACTIF");
    String uri =
        creer(1, "ZEBU", "NOUVEAU", "\"pereId\":\"%s\",".formatted(pere))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.statut").value("ACTIF"))
            .andReturn()
            .getResponse()
            .getHeader("Location");
    mvc.perform(get(uri).with(utilisateur(2)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.identifiant").value("NOUVEAU"));
    org.assertj.core.api.Assertions.assertThat(
            jdbc.queryForObject(
                "select pere_id from animal where identifiant = 'NOUVEAU'", UUID.class))
        .isEqualTo(pere);
  }

  @Test
  void valideLesDroitsEtLeModeDeSuivi() throws Exception {
    creer(2, "ZEBU", "NOUVEAU", "").andExpect(status().isNotFound());
    creer(1, "POULET_CHAIR", "NOUVEAU", "")
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("ESPECE_SUIVIE_EN_LOT"));
  }

  @Test
  void refuseUnParentHorsPerimetre() throws Exception {
    creer(1, "ZEBU", "NOUVEAU", "\"mereId\":\"%s\",".formatted(parent("API-PRIVE")))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("PARENT_INCOMPATIBLE"));
  }

  @Test
  void refuseUnIdentifiantDejaUtilise() throws Exception {
    creer(1, "ZEBU", " api-actif ", "")
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("ANIMAL_IDENTIFIANT_UTILISE"));
  }

  private UUID parent(String identifiant) {
    return jdbc.queryForObject(
        "select id from animal where identifiant = ?", UUID.class, identifiant);
  }

  private ResultActions creer(int numero, String espece, String identifiant, String options)
      throws Exception {
    return mvc.perform(
        post(URL)
            .with(utilisateur(numero))
            .contentType(MediaType.APPLICATION_JSON)
            .content(
                """
                {%s"identifiant":"%s","especeCode":"%s","sexe":"FEMELLE","origine":"NE_SUR_PLACE"}
                """
                    .formatted(options, identifiant, espece)));
  }
}
