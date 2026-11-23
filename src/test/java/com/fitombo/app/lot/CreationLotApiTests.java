package com.fitombo.app.lot;

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
import org.springframework.test.web.servlet.ResultActions;

@Sql("/sql/exploitations-api.sql")
class CreationLotApiTests extends ApiIntegrationTest {
  private static final String URI = "/exploitations/30000000-0000-4000-8000-000000000001/lots";

  @Autowired private JdbcTemplate jdbc;

  @Test
  void creeUnLotActifAvecSonEffectifInitial() throws Exception {
    UUID race =
        jdbc.queryForObject(
            "select id from race where espece_code = 'PORC' and nom = 'Large White'", UUID.class);
    UUID emplacement =
        jdbc.queryForObject(
            """
            insert into emplacement (exploitation_id, nom, type)
            values ('30000000-0000-4000-8000-000000000001', 'Parc du lot', 'PARC') returning id
            """,
            UUID.class);
    creer(1, "PORC", 18, "\"raceId\":\"%s\",\"emplacementId\":\"%s\",".formatted(race, emplacement))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.effectifActuel").value(18))
        .andExpect(jsonPath("$.statut").value("ACTIF"));
    mvc.perform(get(URI).with(utilisateur(2)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items[0].code").value("LOT-A"));
  }

  @Test
  void refuseUnNonProprietaireEtValideLesDonnees() throws Exception {
    creer(2, "PORC", 18, "").andExpect(status().isNotFound());
    creer(1, "PORC", 0, "").andExpect(status().isBadRequest());
    creer(1, "ZEBU", 18, "")
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("ESPECE_NON_SUIVIE_EN_LOT"));
  }

  @Test
  void refuseUneRaceIncompatibleEtUnEmplacementExterieur() throws Exception {
    UUID race =
        jdbc.queryForObject("select id from race where espece_code = 'ZEBU' limit 1", UUID.class);
    creer(1, "PORC", 18, "\"raceId\":\"%s\",".formatted(race))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("RACE_INCOMPATIBLE"));
    UUID emplacement =
        jdbc.queryForObject(
            """
            insert into emplacement (exploitation_id, nom, type)
            values ('30000000-0000-4000-8000-000000000002', 'Parc privé', 'PARC') returning id
            """,
            UUID.class);
    creer(1, "PORC", 18, "\"emplacementId\":\"%s\",".formatted(emplacement))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("EMPLACEMENT_INTROUVABLE"));
  }

  @Test
  void refuseUnCodeDejaUtiliseDansLaFerme() throws Exception {
    creer(1, "PORC", 18, "").andExpect(status().isCreated());
    creer(1, "PORC", 18, "")
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("LOT_CODE_UTILISE"));
  }

  private ResultActions creer(int numero, String espece, int effectif, String options)
      throws Exception {
    return mvc.perform(
        post(URI)
            .with(utilisateur(numero))
            .contentType(MediaType.APPLICATION_JSON)
            .content(
                """
                {"code":" LOT-A ","especeCode":"%s",%s"dateCreation":"2026-01-01","effectifInitial":%d}
                """
                    .formatted(espece, options, effectif)));
  }
}
