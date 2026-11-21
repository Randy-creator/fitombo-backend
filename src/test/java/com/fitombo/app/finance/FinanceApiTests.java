package com.fitombo.app.finance;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fitombo.app.ApiIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

@Sql("/sql/exploitations-api.sql")
class FinanceApiTests extends ApiIntegrationTest {
  private static final String FERME = "/exploitations/30000000-0000-4000-8000-000000000001";

  @Test
  void calculeLaMargeEtReserveLesFinancesAuProprietaire() throws Exception {
    String depense =
        """
        {"categorie":"ALIMENT","montantAriary":40,"dateTransaction":"2026-06-01"}
        """;
    mvc.perform(
            post(FERME + "/transactions")
                .with(utilisateur(2))
                .contentType(MediaType.APPLICATION_JSON)
                .content(depense))
        .andExpect(status().isNotFound());
    mvc.perform(
            post(FERME + "/transactions")
                .with(utilisateur(1))
                .contentType(MediaType.APPLICATION_JSON)
                .content(depense))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.type").value("DEPENSE"));
    mvc.perform(
            post(FERME + "/transactions")
                .with(utilisateur(1))
                .contentType(MediaType.APPLICATION_JSON)
                .content(depense.replace("ALIMENT", "VENTE_LAIT").replace("40", "100")))
        .andExpect(status().isCreated());
    mvc.perform(get(FERME + "/transactions").with(utilisateur(1)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(2));
    mvc.perform(get(FERME + "/marges?du=2026-06-01&au=2026-06-30").with(utilisateur(1)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.margeAriary").value(60));
    mvc.perform(get(FERME + "/marges?du=2026-06-01&au=2026-06-30").with(utilisateur(2)))
        .andExpect(status().isNotFound());
  }
}
