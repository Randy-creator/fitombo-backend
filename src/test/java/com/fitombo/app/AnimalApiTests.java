package com.fitombo.app;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;

@Sql("/sql/exploitations-api.sql")
class AnimalApiTests extends ApiIntegrationTest {

  private static final String ANIMAUX =
      "/exploitations/30000000-0000-4000-8000-000000000001/animaux";

  @Test
  void listeLesAnimauxVisiblesEtFiltreParStatut() throws Exception {
    mvc.perform(get(ANIMAUX).with(utilisateur(2)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(2))
        .andExpect(jsonPath("$.items[0].identifiant").value("API-ACTIF"));

    mvc.perform(get(ANIMAUX).param("statut", "VENDU").with(utilisateur(2)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items.length()").value(1))
        .andExpect(jsonPath("$.items[0].identifiant").value("API-VENDU"));
  }

  @Test
  void protegeLeCheptelEtValideLeStatut() throws Exception {
    mvc.perform(get(ANIMAUX).with(utilisateur(4))).andExpect(status().isNotFound());
    mvc.perform(get(ANIMAUX).param("statut", "INCONNU").with(utilisateur(2)))
        .andExpect(status().isBadRequest());
  }
}
