package com.fitombo.app.evenement;

import static org.assertj.core.api.Assertions.assertThat;
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
import org.springframework.test.web.servlet.ResultActions;

@Sql("/sql/exploitations-api.sql")
class EcritureEvenementApiTests extends ApiIntegrationTest {
  @Autowired private JdbcTemplate jdbc;

  private ResultActions enregistrer(int utilisateur, String type, String details) throws Exception {
    UUID id =
        jdbc.queryForObject("select id from animal where identifiant = 'API-ACTIF'", UUID.class);
    return mvc.perform(
        post("/evenements")
            .with(utilisateur(utilisateur))
            .contentType(MediaType.APPLICATION_JSON)
            .content(
                """
                {"type":"%s","exploitationId":"30000000-0000-4000-8000-000000000001",
                 "animalId":"%s","dateEvenement":"2026-06-01",%s}
                """
                    .formatted(type, id, details)));
  }

  @Test
  void enregistreAlimentationEtControleLesDonnees() throws Exception {
    String details =
        """
        "aliment":"Foin","quantiteKg":12.5,"coutAriary":15000
        """;
    enregistrer(2, "ALIMENTATION", details).andExpect(status().isNotFound());
    enregistrer(1, "ALIMENTATION", details)
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.type").value("ALIMENTATION"));
    assertThat(
            jdbc.queryForObject(
                "select count(*) from alimentation where aliment = ? and quantite_kg = 12.5",
                Integer.class,
                "Foin"))
        .isEqualTo(1);
    enregistrer(
            1,
            "ALIMENTATION",
            """
            "aliment":"Foin","quantiteKg":0
            """)
        .andExpect(status().isBadRequest());
  }

  @Test
  void enregistreVaccinationEtControleLesDonnees() throws Exception {
    String details =
        """
        "vaccin":"Vaccin enregistré","lotVaccin":"DOSE-A","rappelPrevu":"2026-09-01"
        """;
    enregistrer(2, "VACCINATION", details).andExpect(status().isNotFound());
    enregistrer(1, "VACCINATION", details)
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.type").value("VACCINATION"));
    assertThat(
            jdbc.queryForObject(
                "select count(*) from vaccination where vaccin = ? and lot_vaccin = 'DOSE-A'",
                Integer.class,
                "Vaccin enregistré"))
        .isEqualTo(1);
    enregistrer(
            1,
            "VACCINATION",
            """
            "vaccin":"Vaccin enregistré","rappelPrevu":"2026-01-01"
            """)
        .andExpect(status().isBadRequest());
  }

  @Test
  void enregistreProductionEtControleLesDonnees() throws Exception {
    String details =
        """
        "typeProduction":"LAIT","quantite":7.5,"unite":"LITRE"
        """;
    enregistrer(2, "PRODUCTION", details).andExpect(status().isNotFound());
    enregistrer(1, "PRODUCTION", details)
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.type").value("PRODUCTION"));
    assertThat(
            jdbc.queryForObject(
                "select count(*) from production where type_production = ? and quantite = 7.5",
                Integer.class,
                "LAIT"))
        .isEqualTo(1);
    enregistrer(
            1,
            "PRODUCTION",
            """
            "typeProduction":"LAIT","quantite":7.5,"unite":"KG"
            """)
        .andExpect(status().isBadRequest());
  }

  @Test
  void enregistreTraitementEtControleLesDonnees() throws Exception {
    String details =
        """
        "produit":"Soin enregistré","dose":"Dose prescrite","dureeJours":2,"delaiAttenteJours":5
        """;
    enregistrer(2, "TRAITEMENT", details).andExpect(status().isNotFound());
    enregistrer(1, "TRAITEMENT", details)
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.type").value("TRAITEMENT"));
    assertThat(
            jdbc.queryForObject(
                "select count(*) from traitement where produit = ? and fin_delai_attente = date '2026-06-08'",
                Integer.class,
                "Soin enregistré"))
        .isEqualTo(1);
    enregistrer(
            1,
            "TRAITEMENT",
            """
            "produit":"Soin enregistré","delaiAttenteJours":-1
            """)
        .andExpect(status().isBadRequest());
  }
}
