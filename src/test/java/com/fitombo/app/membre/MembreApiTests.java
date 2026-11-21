package com.fitombo.app.membre;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
class MembreApiTests extends ApiIntegrationTest {
  private static final String FERME = "/exploitations/30000000-0000-4000-8000-000000000001";
  @Autowired private JdbcTemplate jdbc;

  @Test
  void invitePuisRevoqueUnCompteApresAcceptation() throws Exception {
    String demande =
        """
        {"utilisateurId":"10000000-0000-4000-8000-000000000004","role":"VETERINAIRE"}
        """;
    mvc.perform(
            post(FERME + "/membres")
                .with(utilisateur(2))
                .contentType(MediaType.APPLICATION_JSON)
                .content(demande))
        .andExpect(status().isNotFound());
    mvc.perform(
            post(FERME + "/membres")
                .with(utilisateur(1))
                .contentType(MediaType.APPLICATION_JSON)
                .content(demande))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.statut").value("INVITE"));
    UUID id =
        jdbc.queryForObject(
            "select id from membre_exploitation where statut = 'INVITE'", UUID.class);
    mvc.perform(get("/me/invitations").with(utilisateur(4)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(1));
    mvc.perform(get(FERME).with(utilisateur(4))).andExpect(status().isNotFound());
    mvc.perform(post("/me/invitations/{id}/acceptation", id).with(utilisateur(2)))
        .andExpect(status().isNotFound());
    mvc.perform(post("/me/invitations/{id}/acceptation", id).with(utilisateur(4)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statut").value("ACTIF"));
    mvc.perform(get(FERME).with(utilisateur(4))).andExpect(status().isOk());
    mvc.perform(delete(FERME + "/membres/{id}", id).with(utilisateur(1)))
        .andExpect(status().isNoContent());
    mvc.perform(get(FERME).with(utilisateur(4))).andExpect(status().isNotFound());
  }
}
