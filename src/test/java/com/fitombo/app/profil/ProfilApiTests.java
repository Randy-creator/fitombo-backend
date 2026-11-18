package com.fitombo.app.profil;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fitombo.app.ApiIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

@Sql("/sql/exploitations-api.sql")
class ProfilApiTests extends ApiIntegrationTest {
  @Test
  void consulteEtModifieLeProfilDuCompteCourant() throws Exception {
    mvc.perform(get("/me").with(utilisateur(2)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.telephone").value("+261340001002"))
        .andExpect(jsonPath("$.motDePasseHash").doesNotExist());

    mvc.perform(
            put("/me")
                .with(utilisateur(2))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"nom":"  Nouveau nom  ","langue":"fr"}
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.nom").value("Nouveau nom"));

    mvc.perform(get("/me").with(utilisateur(2))).andExpect(jsonPath("$.langue").value("fr"));
    mvc.perform(get("/me").with(utilisateur(1)))
        .andExpect(jsonPath("$.nom").value("Propriétaire API"));
  }

  @Test
  void refuseLesDemandesInvalidesEtLesComptesDesactives() throws Exception {
    mvc.perform(
            put("/me")
                .with(utilisateur(2))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"nom":" ","langue":"de"}
                    """))
        .andExpect(status().isBadRequest());
    mvc.perform(get("/me").with(utilisateur(3))).andExpect(status().isNotFound());
  }

  @Test
  void deduitLIdentiteUniquementDuJeton() throws Exception {
    mvc.perform(
            get("/me")
                .param("utilisateurId", "10000000-0000-4000-8000-000000000001")
                .with(utilisateur(2)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.telephone").value("+261340001002"));
    mvc.perform(get("/me").with(jwt().jwt(token -> token.subject("invalide"))))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("UTILISATEUR_NON_IDENTIFIE"));
  }
}
