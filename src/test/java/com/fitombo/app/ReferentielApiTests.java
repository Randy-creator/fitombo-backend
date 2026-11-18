package com.fitombo.app;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReferentielApiTests extends ApiIntegrationTest {

    @Test
    @Sql(statements = """
            insert into espece (code, nom_fr, mode_suivi, actif)
            values ('ZZ_API_MASQUEE', 'Espèce désactivée', 'LOT', false);
            """)
    void listeLesEspecesActivesAvecPagination() throws Exception {
        mvc.perform(get("/especes").param("size", "100").with(utilisateur(1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[*].code", hasItem("ZEBU")))
                .andExpect(jsonPath("$.items[*].code", not(hasItem("ZZ_API_MASQUEE"))));

        mvc.perform(get("/especes").param("size", "1").param("page", "1").with(utilisateur(1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.items.length()").value(1));
    }

    @Test
    @Sql(statements = """
            insert into race (espece_code, nom, actif) values ('ZEBU', 'Race masquée API', false)
            """)
    void listeUniquementLesRacesActivesDeLespece() throws Exception {
        mvc.perform(get("/especes/ZEBU/races").with(utilisateur(1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[*].nom", hasItem("Zébu malgache local")))
                .andExpect(jsonPath("$.items[*].nom", not(hasItem("Race masquée API"))));

        mvc.perform(get("/especes/INCONNUE/races").with(utilisateur(1)))
                .andExpect(status().isNotFound());
    }

    @Test
    void exigeUnJetonEtRefuseUnTriInconnu() throws Exception {
        mvc.perform(get("/especes")).andExpect(status().isUnauthorized());
        mvc.perform(get("/especes").param("sort", "actif").with(utilisateur(1)))
                .andExpect(status().isBadRequest());
    }
}
