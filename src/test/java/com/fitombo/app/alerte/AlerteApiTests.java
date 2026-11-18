package com.fitombo.app.alerte;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fitombo.app.ApiIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

@Sql(
    scripts = "/sql/exploitations-api.sql",
    statements =
        """
        insert into alerte (id, utilisateur_id, exploitation_id, type, statut, date_echeance, message, cle_deduplication) values
          ('40000000-0000-4000-8000-000000000001', '10000000-0000-4000-8000-000000000002', '30000000-0000-4000-8000-000000000001',
            'AUTRE', 'EN_ATTENTE', current_date, 'Alerte visible', 'visible'),
          ('40000000-0000-4000-8000-000000000002', '10000000-0000-4000-8000-000000000002', '30000000-0000-4000-8000-000000000002',
            'AUTRE', 'EN_ATTENTE', current_date, 'Adhésion expirée', 'expiree'),
          ('40000000-0000-4000-8000-000000000003', '10000000-0000-4000-8000-000000000002', null,
            'AUTRE', 'TRAITEE', current_date, 'Alerte globale', 'globale'),
          ('40000000-0000-4000-8000-000000000004', '10000000-0000-4000-8000-000000000001', '30000000-0000-4000-8000-000000000001',
            'AUTRE', 'EN_ATTENTE', current_date, 'Autre destinataire', 'autre');
        """)
class AlerteApiTests extends ApiIntegrationTest {

  private static final String VISIBLE = "/alertes/40000000-0000-4000-8000-000000000001";
  private static final String EXPIREE = "/alertes/40000000-0000-4000-8000-000000000002";
  private static final String TRAITEE = "/alertes/40000000-0000-4000-8000-000000000003";
  private static final String AUTRE = "/alertes/40000000-0000-4000-8000-000000000004";

  @Test
  void listeEtFiltreUniquementLesAlertesVisibles() throws Exception {
    mvc.perform(get("/alertes").with(utilisateur(2)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(2));

    mvc.perform(get("/alertes").param("statut", "EN_ATTENTE").with(utilisateur(2)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(1))
        .andExpect(jsonPath("$.items[0].message").value("Alerte visible"));
  }

  @Test
  void marqueVueDeFaconIdempotenteEtPreserveLesAlertesTraitees() throws Exception {
    for (String uri : new String[] {VISIBLE, VISIBLE, TRAITEE}) {
      mvc.perform(
              patch(uri)
                  .with(utilisateur(2))
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"statut\":\"VUE\"}"))
          .andExpect(status().isNoContent());
    }

    mvc.perform(get("/alertes").param("statut", "VUE").with(utilisateur(2)))
        .andExpect(jsonPath("$.totalElements").value(1))
        .andExpect(jsonPath("$.items[0].message").value("Alerte visible"));
    mvc.perform(get("/alertes").param("statut", "TRAITEE").with(utilisateur(2)))
        .andExpect(jsonPath("$.totalElements").value(1));
  }

  @Test
  void refuseUneAlerteHorsPerimetreOuDestineeAUnAutreUtilisateur() throws Exception {
    for (String uri : new String[] {EXPIREE, AUTRE}) {
      mvc.perform(
              patch(uri)
                  .with(utilisateur(2))
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"statut\":\"VUE\"}"))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.code").value("ALERTE_INTROUVABLE"));
    }
  }
}
