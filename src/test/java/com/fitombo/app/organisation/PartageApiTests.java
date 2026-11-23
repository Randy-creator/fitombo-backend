package com.fitombo.app.organisation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
class PartageApiTests extends ApiIntegrationTest {
  @Autowired private JdbcTemplate jdbc;

  @Test
  void agregatsReservesALOrganisationEtRetiresApresRevocation() throws Exception {
    UUID org =
        jdbc.queryForObject(
            """
            insert into organisation(nom, type) values ('Coopérative test','COOPERATIVE') returning id
            """,
            UUID.class);
    jdbc.update(
        "update utilisateur set role = 'ORGANISATION', organisation_id = ? where telephone = '+261340001002'",
        org);
    jdbc.update(
        """
        insert into utilisateur(id, nom, telephone)
        values ('10000000-0000-4000-8000-000000000005','Éleveur test','+261340001005')
        """);
    jdbc.update(
        """
        insert into eleveur(utilisateur_id) values ('10000000-0000-4000-8000-000000000005')
        """);
    jdbc.update("update eleveur set organisation_id = ?", org);
    partager(1, true, UUID.randomUUID()).andExpect(status().isNotFound());
    partager(1, true, org).andExpect(status().isOk());
    partager(4, true, org).andExpect(status().isOk());
    mvc.perform(get("/organisation/statistiques").with(utilisateur(2)))
        .andExpect(status().isNotFound());
    partager(5, true, org).andExpect(status().isOk());
    mvc.perform(get("/organisation/statistiques").with(utilisateur(1)))
        .andExpect(status().isNotFound());
    mvc.perform(get("/organisation/statistiques").with(utilisateur(2)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.eleveurs").value(3))
        .andExpect(jsonPath("$.exploitations").value(2))
        .andExpect(jsonPath("$.effectifActif").value(2));
    partager(1, false, org)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.partage").value(false));
    mvc.perform(get("/organisation/statistiques").with(utilisateur(2)))
        .andExpect(status().isNotFound());
  }

  private org.springframework.test.web.servlet.ResultActions partager(
      int compte, boolean autorise, UUID org) throws Exception {
    return mvc.perform(
        put("/me/partage-organisation")
            .with(utilisateur(compte))
            .contentType(MediaType.APPLICATION_JSON)
            .content(
                """
                {"partage":%s,"organisationId":"%s"}
                """
                    .formatted(autorise, org)));
  }
}
