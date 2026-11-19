package com.fitombo.app.session;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fitombo.app.ApiIntegrationTest;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.ResultActions;

class SessionApiTests extends ApiIntegrationTest {
  @Autowired private JdbcTemplate jdbc;
  @Autowired private PasswordEncoder motsDePasse;

  @BeforeEach
  void creerCompte() {
    jdbc.update(
        """
        insert into utilisateur (id, nom, telephone, mot_de_passe_hash, telephone_verifie)
        values ('10000000-0000-4000-8000-000000000001', 'Session test', '+261340009111', ?, true)
        """,
        motsDePasse.encode("Session-demo-2026!"));
  }

  @Test
  void changeLeJetonEtRevoqueLaChaineEnCasDeRejeu() throws Exception {
    String initial = connexion("192.0.2.41");
    assertThat(jdbc.queryForObject("select jeton_hash from jeton_rafraichissement", String.class))
        .hasSize(64)
        .isNotEqualTo(initial);
    String json =
        renouveler(initial, "192.0.2.41")
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
            .andReturn()
            .getResponse()
            .getContentAsString();
    String suivant = JsonPath.read(json, "$.refreshToken");
    String acces = JsonPath.read(json, "$.accessToken");
    assertThat(suivant).isNotEqualTo(initial);
    mvc.perform(get("/especes").header(HttpHeaders.AUTHORIZATION, "Bearer " + acces))
        .andExpect(status().isOk());
    renouveler(initial, "192.0.2.41").andExpect(status().isUnauthorized());
    renouveler(suivant, "192.0.2.41").andExpect(status().isUnauthorized());
  }

  @Test
  void refuseUneSessionExpireeOuUnCompteDesactive() throws Exception {
    String expire = connexion("192.0.2.42");
    jdbc.update("update jeton_rafraichissement set expire_at = now() - interval '1 minute'");
    renouveler(expire, "192.0.2.42").andExpect(status().isUnauthorized());
    String actif = connexion("192.0.2.42");
    jdbc.update("update utilisateur set actif = false where telephone = '+261340009111'");
    renouveler(actif, "192.0.2.42").andExpect(status().isUnauthorized());
  }

  String connexion(String adresse) throws Exception {
    String json =
        mvc.perform(
                post("/auth/connexion")
                    .with(requete -> {
                      requete.setRemoteAddr(adresse);
                      return requete;
                    })
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {"telephone":"+261340009111","motDePasse":"Session-demo-2026!"}
                        """))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    return JsonPath.read(json, "$.refreshToken");
  }

  ResultActions renouveler(String jeton, String adresse) throws Exception {
    return mvc.perform(
        post("/auth/refresh")
            .with(requete -> {
              requete.setRemoteAddr(adresse);
              return requete;
            })
            .contentType(MediaType.APPLICATION_JSON)
            .content(corps(jeton)));
  }

  private String corps(String jeton) {
    return "{\"refreshToken\":\"%s\"}".formatted(jeton);
  }

  @Test
  void deconnecteSeulementLaSessionDuTitulaire() throws Exception {
    String premier = connexion("192.0.2.43");

    mvc.perform(
            post("/auth/deconnexion")
                .contentType(MediaType.APPLICATION_JSON)
                .content(corps(premier)))
        .andExpect(status().isUnauthorized());

    mvc.perform(
            post("/auth/deconnexion")
                .with(utilisateur(2))
                .contentType(MediaType.APPLICATION_JSON)
                .content(corps(premier)))
        .andExpect(status().isNoContent());

    String json =
        renouveler(premier, "192.0.2.43")
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    String courant = JsonPath.read(json, "$.refreshToken");
    String acces = JsonPath.read(json, "$.accessToken");
    for (int appel = 0; appel < 2; appel++) {
      mvc.perform(
              post("/auth/deconnexion")
                  .header(HttpHeaders.AUTHORIZATION, "Bearer " + acces)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(corps(courant)))
          .andExpect(status().isNoContent());
    }

    renouveler(courant, "192.0.2.43").andExpect(status().isUnauthorized());
  }
}
