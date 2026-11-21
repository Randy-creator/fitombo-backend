package com.fitombo.app.emplacement;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import org.springframework.test.web.servlet.ResultActions;

@Sql("/sql/exploitations-api.sql")
class CreationEmplacementApiTests extends ApiIntegrationTest {
  private static final String URI =
      "/exploitations/30000000-0000-4000-8000-000000000001/emplacements";

  @Test
  void leProprietaireCreeUnEmplacementConsultableParLeMembre() throws Exception {
    creer(1, "Parc neuf", 8)
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.capacite").value(8));
    mvc.perform(get(URI).with(utilisateur(2)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items[0].nom").value("Parc neuf"));
  }

  @Test
  void limiteLaCreationAuProprietaireEtValideLaCapacite() throws Exception {
    creer(2, "Parc neuf", 8).andExpect(status().isNotFound());
    creer(4, "Parc neuf", 8).andExpect(status().isNotFound());
    creer(1, "Parc neuf", -1).andExpect(status().isBadRequest());
  }

  @Test
  void refuseUnNomDejaUtiliseDansLaFerme() throws Exception {
    creer(1, "Parc neuf", 8).andExpect(status().isCreated());
    creer(1, " parc neuf ", 8)
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("EMPLACEMENT_NOM_UTILISE"));
  }

  private ResultActions creer(int numero, String nom, int capacite) throws Exception {
    return mvc.perform(
        post(URI)
            .with(utilisateur(numero))
            .contentType(MediaType.APPLICATION_JSON)
            .content(
                """
                {"nom":"%s","type":"PARC","capacite":%d}
                """
                    .formatted(nom, capacite)));
  }

  @Autowired private JdbcTemplate jdbc;

  @Test
  void modifieEtArchiveUnEmplacementPourLeProprietaire() throws Exception {
    String base = "/exploitations/30000000-0000-4000-8000-000000000001/emplacements";
    String demande =
        """
        {"nom":"Enclos à modifier","type":"ENCLOS","capacite":15}
        """;
    mvc.perform(
            post(base)
                .with(utilisateur(1))
                .contentType(MediaType.APPLICATION_JSON)
                .content(demande))
        .andExpect(status().isCreated());
    UUID id =
        jdbc.queryForObject(
            "select id from emplacement where nom = ?", UUID.class, "Enclos à modifier");
    mvc.perform(
            put("/emplacements/{id}", id)
                .with(utilisateur(2))
                .contentType(MediaType.APPLICATION_JSON)
                .content(demande))
        .andExpect(status().isNotFound());
    mvc.perform(
            put("/emplacements/{id}", id)
                .with(utilisateur(1))
                .contentType(MediaType.APPLICATION_JSON)
                .content(demande.replace("15", "25")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.capacite").value(25));
    mvc.perform(delete("/emplacements/{id}", id).with(utilisateur(1)))
        .andExpect(status().isNoContent());
    mvc.perform(
            put("/emplacements/{id}", id)
                .with(utilisateur(1))
                .contentType(MediaType.APPLICATION_JSON)
                .content(demande))
        .andExpect(status().isNotFound());
  }
}
