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
class FicheAnimalApiTests extends ApiIntegrationTest {
  @Autowired private JdbcTemplate jdbc;

  @Test
  void consulteUnAnimalVisibleMemeApresSaVente() throws Exception {
    mvc.perform(get(uri("API-VENDU")).with(utilisateur(2)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.identifiant").value("API-VENDU"))
        .andExpect(jsonPath("$.statut").value("VENDU"));
  }

  @Test
  void masqueUnAnimalSupprimeOuHorsPerimetre() throws Exception {
    for (String identifiant : new String[] {"API-SUPPRIME", "API-PRIVE"}) {
      mvc.perform(get(uri(identifiant)).with(utilisateur(2)))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.code").value("ANIMAL_INTROUVABLE"));
    }
  }

  private String uri(String identifiant) {
    UUID id =
        jdbc.queryForObject("select id from animal where identifiant = ?", UUID.class, identifiant);
    return "/animaux/" + id;
  }
}
