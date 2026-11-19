package com.fitombo.app.objectif;

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
class ObjectifApiTests extends ApiIntegrationTest {
  private static final String EXPLOITATION = "30000000-0000-4000-8000-000000000001";
  @Autowired private JdbcTemplate jdbc;

  private UUID animal() {
    return jdbc.queryForObject("select id from animal where identifiant = 'API-ACTIF'", UUID.class);
  }

  private void peser(UUID animal, String date, int poids) throws Exception {
    mvc.perform(
            post("/evenements")
                .with(utilisateur(1))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"type":"PESEE","exploitationId":"%s","animalId":"%s",
                     "dateEvenement":"%s","methode":"BALANCE","poidsKg":%d}
                    """
                        .formatted(EXPLOITATION, animal, date, poids)))
        .andExpect(status().isCreated());
  }

  @Test
  void calculeLaProgressionEtLaDateDepuisLesPeseesValides() throws Exception {
    UUID animal = animal();
    peser(animal, "2026-06-01", 100);
    peser(animal, "2026-06-11", 110);
    String route = "/objectifs?exploitationId=" + EXPLOITATION;
    String demande =
        """
        {"animalId":"%s","type":"POIDS_CIBLE","unite":"KG","valeurCible":120,
         "dateDebut":"2026-06-01","dateCible":"2026-12-01"}
        """
            .formatted(animal);
    mvc.perform(
            post(route)
                .with(utilisateur(2))
                .contentType(MediaType.APPLICATION_JSON)
                .content(demande))
        .andExpect(status().isNotFound());
    mvc.perform(
            post(route)
                .with(utilisateur(1))
                .contentType(MediaType.APPLICATION_JSON)
                .content(demande))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.valeurInitiale").value(100))
        .andExpect(jsonPath("$.progressionPourcent").value(50))
        .andExpect(jsonPath("$.datePrevisionnelle").value("2026-06-21"));
    peser(animal, "2026-06-21", 120);
    mvc.perform(get(route).with(utilisateur(1)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items[0].statut").value("ATTEINT"));
  }

  @Test
  void mesureUnObjectifEffectifEtGardeLesFinancesPrivees() throws Exception {
    String route = "/objectifs?exploitationId=" + EXPLOITATION;
    String effectif =
        """
        {"type":"EFFECTIF_CIBLE","unite":"TETES","valeurCible":5,
         "dateDebut":"2026-06-01","dateCible":"2026-12-01"}
        """;
    mvc.perform(
            post(route)
                .with(utilisateur(1))
                .contentType(MediaType.APPLICATION_JSON)
                .content(effectif))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.valeurActuelle").value(1));
    mvc.perform(get(route).with(utilisateur(2))).andExpect(status().isNotFound());
    jdbc.update(
        """
        insert into transaction_financiere(exploitation_id,type,categorie,montant_ariary,date_transaction)
        values (cast(? as uuid),'RECETTE','VENTE_LAIT',100,'2026-06-11'),
          (cast(? as uuid),'DEPENSE','ALIMENT',40,'2026-06-11')
        """,
        EXPLOITATION,
        EXPLOITATION);
    mvc.perform(
            post(route)
                .with(utilisateur(1))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    effectif.replace("EFFECTIF_CIBLE", "MARGE_CIBLE").replace("TETES", "POURCENT")))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.valeurActuelle").value(60));
  }
}
