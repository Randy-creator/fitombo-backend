package com.fitombo.app.inscription;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fitombo.app.ApiIntegrationTest;
import com.fitombo.app.service.inscription.TransportSms;
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
  @MockitoBean private TransportSms sms;
  @Autowired private JdbcTemplate jdbc;

  @Test
  void verifieLeTelephoneCreeLeProfilEtAutoriseLeLogin() throws Exception {
    String numero = "+261340009001";
    var demande = demander(numero, "192.0.2.31");
    assertThat(
            jdbc.queryForObject(
                "select count(*) from utilisateur where telephone = ?", Integer.class, numero))
        .isZero();
    verifier(numero, demande.id(), demande.code(), "192.0.2.31").andExpect(status().isCreated());
    verifier(numero, demande.id(), demande.code(), "192.0.2.31")
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("SMS_INVALIDE"));
    assertThat(
            jdbc.queryForObject(
                """
                select count(*) from eleveur e join utilisateur u on u.id = e.utilisateur_id
                where telephone = ? and telephone_verifie
                """,
                Integer.class,
                numero))
        .isEqualTo(1);
    mvc.perform(
            post("/auth/connexion")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"telephone":"%s","motDePasse":"Inscription-demo-2026!"}
                    """
                        .formatted(numero)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").exists());
  }

  @Test
  void limiteLeRenvoiEtLeNombreDeCodesErrones() throws Exception {
    String numero = "+261340009002";
    var demande = demander(numero, "192.0.2.32");
    requete("/auth/inscription", "{\"telephone\":\"" + numero + "\"}", "192.0.2.32")
        .andExpect(status().isTooManyRequests());
    String faux = demande.code().equals("111111") ? "222222" : "111111";
    for (int essai = 0; essai < 5; essai++) {
      verifier(numero, demande.id(), faux, "192.0.2.32").andExpect(status().isBadRequest());
    }
    verifier(numero, demande.id(), demande.code(), "192.0.2.32").andExpect(status().isBadRequest());
    assertThat(
            jdbc.queryForObject(
                "select tentatives from code_verification where id = ?",
                Integer.class,
                UUID.fromString(demande.id())))
        .isEqualTo(5);
  }

  @Test
  void refuseUnCodeExpire() throws Exception {
    String numero = "+261340009003";
    var demande = demander(numero, "192.0.2.33");
    jdbc.update(
        "update code_verification set expire_at = now() - interval '1 minute' where id = ?",
        UUID.fromString(demande.id()));
    verifier(numero, demande.id(), demande.code(), "192.0.2.33").andExpect(status().isBadRequest());
  }

  private Demande demander(String numero, String adresse) throws Exception {
    String json =
        requete("/auth/inscription", "{\"telephone\":\"" + numero + "\"}", adresse)
            .andExpect(status().isAccepted())
            .andExpect(jsonPath("$.code").doesNotExist())
            .andReturn()
            .getResponse()
            .getContentAsString();
    var code = ArgumentCaptor.forClass(String.class);
    verify(sms).envoyer(org.mockito.ArgumentMatchers.eq(numero), code.capture());
    return new Demande(JsonPath.read(json, "$.demandeId"), code.getValue());
  }

  private ResultActions verifier(String numero, String id, String code, String adresse)
      throws Exception {
    return requete(
        "/auth/verification-sms",
        """
        {"demandeId":"%s","telephone":"%s","code":"%s","nom":"Tiana",
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
