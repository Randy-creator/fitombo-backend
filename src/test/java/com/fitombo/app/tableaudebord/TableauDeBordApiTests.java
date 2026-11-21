package com.fitombo.app.tableaudebord;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fitombo.app.ApiIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;

@Sql("/sql/exploitations-api.sql")
class TableauDeBordApiTests extends ApiIntegrationTest {
  @Test
  void presenteLesIndicateursAuProprietaire() throws Exception {
    String url = "/exploitations/30000000-0000-4000-8000-000000000001/tableau-de-bord";
    mvc.perform(get(url).with(utilisateur(1)).param("du", "2026-06-01").param("au", "2026-06-30"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.effectifs[0].especeCode").value("ZEBU"))
        .andExpect(jsonPath("$.effectifs[0].effectif").value(1))
        .andExpect(jsonPath("$.finances.margeAriary").value(0));
    mvc.perform(get(url).with(utilisateur(2)).param("du", "2026-06-01").param("au", "2026-06-30"))
        .andExpect(status().isNotFound());
  }
}
