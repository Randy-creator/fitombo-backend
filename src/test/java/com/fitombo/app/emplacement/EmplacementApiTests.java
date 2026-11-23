package com.fitombo.app.emplacement;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fitombo.app.ApiIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;

@Sql(
    scripts = "/sql/exploitations-api.sql",
    statements =
        """
        insert into emplacement (exploitation_id, nom, type, capacite, deleted_at) values
        ('30000000-0000-4000-8000-000000000001', 'Enclos API', 'ENCLOS', 12, null),
        ('30000000-0000-4000-8000-000000000001', 'Ancien enclos', 'ENCLOS', 10, now()),
        ('30000000-0000-4000-8000-000000000002', 'Enclos privé', 'ENCLOS', 10, null);
        """)
class EmplacementApiTests extends ApiIntegrationTest {
  private static final String URI =
      "/exploitations/30000000-0000-4000-8000-000000000001/emplacements";

  @Test
  void listeLesElementsVisiblesPourUnMembreActif() throws Exception {
    mvc.perform(get(URI).with(utilisateur(2)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(1))
        .andExpect(jsonPath("$.items[0].nom").value("Enclos API"));
  }

  @Test
  void masqueLesElementsAuxNonMembres() throws Exception {
    mvc.perform(get(URI).with(utilisateur(4))).andExpect(status().isNotFound());
  }
}
