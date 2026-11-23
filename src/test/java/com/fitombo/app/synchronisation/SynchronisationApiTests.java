package com.fitombo.app.synchronisation;

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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Sql(
    value = "/sql/exploitations-api.sql",
    config =
        @org.springframework.test.context.jdbc.SqlConfig(
            transactionMode =
                org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED))
@Sql(
    statements = {
      "delete from reception_sync_evenement where utilisateur_id = '10000000-0000-4000-8000-000000000001'",
      "delete from evenement where saisi_par = '10000000-0000-4000-8000-000000000001'",
      "delete from animal where identifiant in ('API-ACTIF','API-VENDU','API-SUPPRIME','API-PRIVE')",
      "delete from membre_exploitation where exploitation_id in (select id from exploitation where nom in ('Ferme API','Ferme privée API','Ferme supprimée API'))",
      "delete from exploitation where nom in ('Ferme API','Ferme privée API','Ferme supprimée API')",
      "delete from eleveur where utilisateur_id in ('10000000-0000-4000-8000-000000000001','10000000-0000-4000-8000-000000000004')",
      "delete from utilisateur where telephone in ('+261340001001','+261340001002','+261340001003','+261340001004')"
    },
    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
    config =
        @org.springframework.test.context.jdbc.SqlConfig(
            transactionMode =
                org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED))
class SynchronisationApiTests extends ApiIntegrationTest {
  @Autowired private JdbcTemplate jdbc;

  @Test
  void accepteUneSaisieRejoueeSansRecreerLEvenement() throws Exception {
    UUID animal =
        jdbc.queryForObject("select id from animal where identifiant = 'API-ACTIF'", UUID.class);
    String operation =
        """
        {"operationId":"%s","saisie":{"type":"PESEE","exploitationId":"30000000-0000-4000-8000-000000000001",
        "animalId":"%s","dateEvenement":"2026-06-01","methode":"BALANCE","poidsKg":125}}
        """
            .formatted(UUID.randomUUID(), animal);
    String lot = "{\"operations\":[" + operation + "]}";
    mvc.perform(
            post("/sync/push")
                .with(utilisateur(2))
                .contentType(MediaType.APPLICATION_JSON)
                .content(lot))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].statut").value("REJETE"));
    mvc.perform(
            post("/sync/push")
                .with(utilisateur(1))
                .contentType(MediaType.APPLICATION_JSON)
                .content(lot))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].statut").value("ACCEPTE"));
    mvc.perform(
            post("/sync/push")
                .with(utilisateur(1))
                .contentType(MediaType.APPLICATION_JSON)
                .content(lot))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].statut").value("DOUBLON"));
    mvc.perform(
            post("/sync/push")
                .with(utilisateur(1))
                .contentType(MediaType.APPLICATION_JSON)
                .content(lot.replace("125", "126")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].code").value("OPERATION_SYNC_REUTILISEE"));
    assertThat(
            jdbc.queryForObject(
                "select count(*) from evenement where animal_id = ?", Integer.class, animal))
        .isEqualTo(1);
  }
}
