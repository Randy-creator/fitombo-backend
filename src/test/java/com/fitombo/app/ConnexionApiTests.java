package com.fitombo.app;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.ResultActions;

import com.jayway.jsonpath.JsonPath;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ConnexionApiTests extends ApiIntegrationTest {

    private static final UUID UTILISATEUR_ID = UUID.fromString("10000000-0000-4000-8000-000000000010");
    private static final String TELEPHONE = "+261340001010";
    private static final String MOT_DE_PASSE = "mot-de-passe-de-test";

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private PasswordEncoder motsDePasse;

    @Autowired
    private JwtDecoder decoder;

    @BeforeEach
    void creerCompte() {
        jdbc.update("""
                insert into utilisateur (id, nom, telephone, mot_de_passe_hash, telephone_verifie)
                values (?, 'Compte de test', ?, ?, true)
                """, UTILISATEUR_ID, TELEPHONE, motsDePasse.encode(MOT_DE_PASSE));
    }

    @Test
    void connecteUnCompteActifEtEmetUnJetonUtilisable() throws Exception {
        String json = connecter(TELEPHONE, MOT_DE_PASSE, "192.0.2.10")
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(1800))
                .andReturn().getResponse().getContentAsString();

        String token = JsonPath.read(json, "$.accessToken");
        var jwt = decoder.decode(token);
        assertThat(jwt.getSubject()).isEqualTo(UTILISATEUR_ID.toString());
        assertThat(jwt.getClaimAsString("role")).isEqualTo("ELEVEUR");
        assertThat(jwt.getClaimAsString("token_use")).isEqualTo("access");

        mvc.perform(get("/especes").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void refuseLeMauvaisMotDePasseEtLeCompteInexistantAvecLaMemeErreur() throws Exception {
        connecter(TELEPHONE, "incorrect", "192.0.2.11")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("IDENTIFIANTS_INVALIDES"));
        connecter("+261340001011", MOT_DE_PASSE, "192.0.2.11")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("IDENTIFIANTS_INVALIDES"));
    }

    @Test
    void refuseLesComptesNonEligibles() throws Exception {
        for (String etat : new String[]{
                "actif = false, telephone_verifie = true, deleted_at = null",
                "actif = true, telephone_verifie = false, deleted_at = null",
                "actif = true, telephone_verifie = true, deleted_at = now()",
                "actif = true, telephone_verifie = true, deleted_at = null, mot_de_passe_hash = null"
        }) {
            jdbc.update("update utilisateur set " + etat + " where id = ?", UTILISATEUR_ID);
            connecter(TELEPHONE, MOT_DE_PASSE, "192.0.2.12").andExpect(status().isUnauthorized());
        }
    }

    @Test
    void rejetteUneRequeteInvalideSansExposerLeMotDePasse() throws Exception {
        String motDePasse = "secret".repeat(15);
        connecter("!", motDePasse, "192.0.2.13")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("CONNEXION_INVALIDE"))
                .andExpect(content().string(not(containsString(motDePasse))));
    }

    @Test
    void limiteLesTentativesParAdresse() throws Exception {
        for (int tentative = 0; tentative < 10; tentative++) {
            connecter(TELEPHONE, MOT_DE_PASSE, "192.0.2.14").andExpect(status().isOk());
        }
        connecter(TELEPHONE, MOT_DE_PASSE, "192.0.2.14")
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value("LIMITE_CONNEXION_ATTEINTE"));
        connecter(TELEPHONE, MOT_DE_PASSE, "192.0.2.15").andExpect(status().isOk());
    }

    private ResultActions connecter(String telephone, String motDePasse, String adresse) throws Exception {
        return mvc.perform(post("/auth/connexion")
                .with(requete -> {
                    requete.setRemoteAddr(adresse);
                    return requete;
                })
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"telephone":"%s","motDePasse":"%s"}
                        """.formatted(telephone, motDePasse)));
    }
}
