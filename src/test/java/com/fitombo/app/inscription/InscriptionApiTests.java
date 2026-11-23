package com.fitombo.app.inscription;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fitombo.app.ApiIntegrationTest;
import com.fitombo.app.service.inscription.TransportEmail;
import com.jayway.jsonpath.JsonPath;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

class InscriptionApiTests extends ApiIntegrationTest {
  @MockitoBean private TransportEmail courriel;
  @Autowired private JdbcTemplate jdbc;
  @Autowired private org.springframework.security.crypto.password.PasswordEncoder motsDePasse;

  @Test
  void verifieLEmailCreeLeProfilEtAutoriseLeLogin() throws Exception {
    String numero = "tiana@example.test";
    var demande = demander(numero, "192.0.2.31");
    assertThat(
            jdbc.queryForObject(
                "select count(*) from utilisateur where email = ?", Integer.class, numero))
        .isZero();
    verifier(numero, demande.id(), demande.code(), "192.0.2.31").andExpect(status().isCreated());
    verifier(numero, demande.id(), demande.code(), "192.0.2.31")
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("CODE_EMAIL_INVALIDE"));
    assertThat(
            jdbc.queryForObject(
                """
                select count(*) from eleveur e join utilisateur u on u.id = e.utilisateur_id
                where email = ? and email_verifie and telephone is null
                """,
                Integer.class,
                numero))
        .isEqualTo(1);
    String connexion =
        mvc.perform(
                post("/auth/connexion")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {"email":"%s","motDePasse":"Inscription-demo-2026!"}
                        """
                            .formatted(numero.toUpperCase(java.util.Locale.ROOT))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").exists())
            .andReturn()
            .getResponse()
            .getContentAsString();
    String jeton = JsonPath.read(connexion, "$.refreshToken");
    requete(
            "/auth/refresh",
            """
            {"refreshToken":"%s"}
            """
                .formatted(jeton),
            "192.0.2.31")
        .andExpect(status().isOk());
  }

  @Test
  void limiteLeRenvoiEtLeNombreDeCodesErrones() throws Exception {
    String numero = "quota@example.test";
    var demande = demander(numero, "192.0.2.32");
    requete("/auth/inscription", "{\"email\":\"" + numero + "\"}", "192.0.2.32")
        .andExpect(status().isTooManyRequests());
    String faux = demande.code().equals("111111") ? "222222" : "111111";
    for (int essai = 0; essai < 5; essai++) {
      verifier(numero, demande.id(), faux, "192.0.2.32").andExpect(status().isBadRequest());
    }
    verifier(numero, demande.id(), demande.code(), "192.0.2.32").andExpect(status().isBadRequest());
    assertThat(
            jdbc.queryForObject(
                "select tentatives from code_verification_email where id = ?",
                Integer.class,
                UUID.fromString(demande.id())))
        .isEqualTo(5);
  }

  @Test
  void refuseUnCodeExpire() throws Exception {
    String numero = "expiration@example.test";
    var demande = demander(numero, "192.0.2.33");
    jdbc.update(
        "update code_verification_email set expire_at = now() - interval '1 minute' where id = ?",
        UUID.fromString(demande.id()));
    verifier(numero, demande.id(), demande.code(), "192.0.2.33").andExpect(status().isBadRequest());
  }

  @Test
  void refuseLaConnexionAvecUnEmailNonVerifie() throws Exception {
    jdbc.update(
        """
        insert into utilisateur (nom, email, mot_de_passe_hash)
        values ('Compte non vérifié', 'non-verifie@example.test', ?)
        """,
        motsDePasse.encode("Inscription-demo-2026!"));
    mvc.perform(
            post("/auth/connexion")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"email":"non-verifie@example.test","motDePasse":"Inscription-demo-2026!"}
                    """))
        .andExpect(status().isUnauthorized());
  }

  private Demande demander(String numero, String adresse) throws Exception {
    String json =
        requete("/auth/inscription", "{\"email\":\"" + numero + "\"}", adresse)
            .andExpect(status().isAccepted())
            .andExpect(jsonPath("$.code").doesNotExist())
            .andReturn()
            .getResponse()
            .getContentAsString();
    var code = ArgumentCaptor.forClass(String.class);
    verify(courriel).envoyer(org.mockito.ArgumentMatchers.eq(numero), code.capture());
    return new Demande(JsonPath.read(json, "$.demandeId"), code.getValue());
  }

  private ResultActions verifier(String numero, String id, String code, String adresse)
      throws Exception {
    return requete(
        "/auth/verification-email",
        """
        {"demandeId":"%s","email":"%s","code":"%s","nom":"Tiana",
         "langue":"mg","motDePasse":"Inscription-demo-2026!"}
        """
            .formatted(id, numero, code),
        adresse);
  }

  private ResultActions requete(String chemin, String corps, String adresse) throws Exception {
    return mvc.perform(
        post(chemin)
            .with(requete -> {
              requete.setRemoteAddr(adresse);
              return requete;
            })
            .contentType(MediaType.APPLICATION_JSON)
            .content(corps));
  }

  private record Demande(String id, String code) {}
}
