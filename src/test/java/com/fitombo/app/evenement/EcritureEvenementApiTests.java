package com.fitombo.app.evenement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fitombo.app.ApiIntegrationTest;
import jakarta.persistence.EntityManager;
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
  @Autowired private EntityManager entites;

  private ResultActions enregistrer(int utilisateur, String type, String details) throws Exception {
    return enregistrerPour(utilisateur, type, trouverAnimal("API-ACTIF"), details);
  }

  private ResultActions enregistrerPour(int utilisateur, String type, UUID id, String details)
      throws Exception {
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

  @Test
  void annuleUnEvenementEtConserveLePremierMotif() throws Exception {
    enregistrer(
            1,
            "ALIMENTATION",
            """
            "aliment":"Saisie à annuler","quantiteKg":5
            """)
        .andExpect(status().isCreated());
    UUID id =
        jdbc.queryForObject(
            "select evenement_id from alimentation where aliment = ?",
            UUID.class,
            "Saisie à annuler");

    annuler(2, id, "Saisie en double").andExpect(status().isNotFound());
    annuler(1, id, " ").andExpect(status().isBadRequest());
    annuler(1, id, "Saisie en double").andExpect(status().isNoContent());
    annuler(1, id, "Autre motif").andExpect(status().isNoContent());

    assertThat(jdbc.queryForMap("select statut, motif_annulation from evenement where id = ?", id))
        .containsEntry("statut", "ANNULE")
        .containsEntry("motif_annulation", "Saisie en double");
  }

  @Test
  void corrigeUneSaisieEnGardantLaTraceDeLOriginal() throws Exception {
    enregistrer(
            1,
            "ALIMENTATION",
            """
            "aliment":"Saisie à corriger","quantiteKg":5
            """)
        .andExpect(status().isCreated());
    UUID original =
        jdbc.queryForObject(
            "select evenement_id from alimentation where aliment = ?",
            UUID.class,
            "Saisie à corriger");
    UUID animal =
        jdbc.queryForObject("select animal_id from evenement where id = ?", UUID.class, original);
    entites.clear();
    jdbc.update(
        "update animal set statut = 'VENDU', date_sortie = '2026-06-15' where id = ?", animal);

    corriger(original, UUID.randomUUID()).andExpect(status().isBadRequest());
    assertThat(
            jdbc.queryForObject(
                "select statut from evenement where id = ?", String.class, original))
        .isEqualTo("VALIDE");

    corriger(original, animal).andExpect(status().isCreated());
    corriger(original, animal).andExpect(status().isConflict());
    assertThat(
            jdbc.queryForMap(
                "select statut, motif_annulation from evenement where id = ?", original))
        .containsEntry("statut", "ANNULE")
        .containsEntry("motif_annulation", "Quantité erronée");
    assertThat(
            jdbc.queryForObject(
                """
                select count(*) from evenement join alimentation on evenement_id = id
                where corrige_evenement_id = ? and statut = 'VALIDE' and quantite_kg = 7
                """,
                Integer.class,
                original))
        .isEqualTo(1);
  }

  private ResultActions corriger(UUID original, UUID animal) throws Exception {
    return mvc.perform(
        put("/evenements/{id}", original)
            .with(utilisateur(1))
            .contentType(MediaType.APPLICATION_JSON)
            .content(
                """
                {"motif":"Quantité erronée","saisie":{"type":"ALIMENTATION",
                 "exploitationId":"30000000-0000-4000-8000-000000000001","animalId":"%s",
                 "dateEvenement":"2026-06-01","aliment":"Saisie corrigée","quantiteKg":7}}
                """
                    .formatted(animal)));
  }

  @Test
  void enregistreUneVaccinationPourChaqueAnimalDeLaSaisieGroupee() throws Exception {
    UUID actif = trouverAnimal("API-ACTIF");
    UUID vendu = trouverAnimal("API-VENDU");

    vaccinationGroupee(actif, actif).andExpect(status().isBadRequest());
    vaccinationGroupee(actif, vendu).andExpect(status().isConflict());
    assertThat(compterVaccinationsGroupees()).isZero();

    jdbc.update("update animal set statut = 'ACTIF', date_sortie = null where id = ?", vendu);
    entites.clear();
    vaccinationGroupee(actif, vendu)
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.length()").value(2));
    assertThat(compterVaccinationsGroupees()).isEqualTo(2);
  }

  private UUID trouverAnimal(String identifiant) {
    return jdbc.queryForObject(
        "select id from animal where identifiant = ?", UUID.class, identifiant);
  }

  private int compterVaccinationsGroupees() {
    return jdbc.queryForObject(
        "select count(*) from vaccination where vaccin = ?", Integer.class, "Vaccination groupée");
  }

  private ResultActions vaccinationGroupee(UUID premier, UUID second) throws Exception {
    return mvc.perform(
        post("/evenements/groupe")
            .with(utilisateur(1))
            .contentType(MediaType.APPLICATION_JSON)
            .content(
                """
                {"animalIds":["%s","%s"],"saisie":{"type":"VACCINATION",
                  "exploitationId":"30000000-0000-4000-8000-000000000001",
                  "dateEvenement":"2026-06-01","vaccin":"Vaccination groupée"}}
                """
                    .formatted(premier, second)));
  }

  @Test
  void constateUnDecesEtRetireLAnimalDuCheptelActif() throws Exception {
    String details =
        """
        "cause":"VOL","nombre":1
        """;
    enregistrer(2, "DECES", details).andExpect(status().isNotFound());
    enregistrer(1, "DECES", details).andExpect(status().isCreated());
    assertThat(
            jdbc.queryForObject(
                "select statut from animal where id = ?", String.class, trouverAnimal("API-ACTIF")))
        .isEqualTo("VOLE");
    enregistrer(1, "DECES", details).andExpect(status().isConflict());
  }

  @Test
  void suitUneGestationEtCreeUnPetitRattacheAuxParents() throws Exception {
    UUID male = trouverAnimal("API-ACTIF");
    UUID femelle = trouverAnimal("API-VENDU");
    jdbc.update("update animal set statut = 'ACTIF', date_sortie = null where id = ?", femelle);

    enregistrerPour(
            1,
            "SAILLIE",
            male,
            """
            "methode":"MONTE_NATURELLE"
            """)
        .andExpect(status().isBadRequest());
    enregistrerPour(
            1,
            "SAILLIE",
            femelle,
            """
            "methode":"MONTE_NATURELLE","maleId":"%s"
            """
                .formatted(male))
        .andExpect(status().isCreated());
    UUID saillie = jdbc.queryForObject("select evenement_id from saillie", UUID.class);
    assertThat(
            jdbc.queryForObject(
                """
                select s.mise_bas_prevue = e.date_evenement + es.duree_gestation_jours
                from saillie s join evenement e on e.id = s.evenement_id
                join animal a on a.id = e.animal_id join espece es on es.code = a.espece_code
                where s.evenement_id = ?
                """,
                Boolean.class,
                saillie))
        .isTrue();

    enregistrerPour(
            1,
            "DIAGNOSTIC_GESTATION",
            femelle,
            """
            "methode":"ECHOGRAPHIE","positif":true,"saillieId":"%s"
            """
                .formatted(saillie))
        .andExpect(status().isCreated());
    enregistrerPour(
            1,
            "MISE_BAS",
            femelle,
            """
            "nesVivants":1,"mortsNes":0,"saillieId":"%s",
            "petits":[{"identifiant":"API-NOUVEAU-NE","sexe":"FEMELLE"}]
            """
                .formatted(saillie))
        .andExpect(status().isCreated());

    assertThat(
            jdbc.queryForMap(
                """
                select a.mere_id, a.pere_id, m.nes_vivants from animal a
                join mise_bas m on m.evenement_id = a.mise_bas_id where a.identifiant = ?
                """,
                "API-NOUVEAU-NE"))
        .containsEntry("mere_id", femelle)
        .containsEntry("pere_id", male)
        .containsEntry("nes_vivants", 1);
  }

  @Test
  void vendUnAnimalEtSignaleLeDelaiAttente() throws Exception {
    enregistrer(
            1,
            "TRAITEMENT",
            """
            "produit":"Soin avant vente","delaiAttenteJours":10
            """)
        .andExpect(status().isCreated());
    String vente =
        """
        "typeMouvement":"VENTE","quantite":1,"nomTiers":"Acheteur test",
        "prixTotalAriary":500000,"poidsTotalKg":200,"numeroPieceCirculation":"PIECE-TEST"
        """;
    enregistrer(2, "MOUVEMENT", vente).andExpect(status().isNotFound());
    enregistrer(1, "MOUVEMENT", vente)
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.delaiAttenteJusquAu").value("2026-06-11"));
    assertThat(
            jdbc.queryForObject(
                "select statut from animal where id = ?", String.class, trouverAnimal("API-ACTIF")))
        .isEqualTo("VENDU");
  }

  @Test
  void confieUnAnimalPuisMetFinAuConfiage() throws Exception {
    enregistrer(
            1,
            "MOUVEMENT",
            """
            "typeMouvement":"CONFIAGE","quantite":1,
            "gardienId":"20000000-0000-4000-8000-000000000004","conditions":"Garde temporaire"
            """)
        .andExpect(status().isCreated());
    enregistrer(
            1,
            "MOUVEMENT",
            """
            "typeMouvement":"RETOUR_CONFIAGE","quantite":1
            """)
        .andExpect(status().isCreated());
    assertThat(
            jdbc.queryForObject(
                """
                select count(*) from propriete where type = 'GARDIEN'
                  and conditions = 'Garde temporaire' and date_fin is not null
                """,
                Integer.class))
        .isEqualTo(1);
  }

  @Test
  void transfereUnAnimalEtTraceLesDeuxExploitations() throws Exception {
    UUID destination =
        jdbc.queryForObject(
            """
            insert into exploitation(eleveur_id, nom)
            values ('20000000-0000-4000-8000-000000000001', 'Destination transfert')
            returning id
            """,
            UUID.class);
    UUID animal = trouverAnimal("API-ACTIF");
    enregistrer(
            1,
            "MOUVEMENT",
            """
            "typeMouvement":"TRANSFERT","quantite":1,
            "exploitationDestinationId":"30000000-0000-4000-8000-000000000002"
            """)
        .andExpect(status().isNotFound());
    enregistrer(
            1,
            "MOUVEMENT",
            """
            "typeMouvement":"TRANSFERT","quantite":1,"exploitationDestinationId":"%s"
            """
                .formatted(destination))
        .andExpect(status().isCreated());
    UUID evenement =
        jdbc.queryForObject(
            """
            select id from evenement where animal_id = ? and type = 'MOUVEMENT'
            """,
            UUID.class,
            animal);
    assertThat(
            jdbc.queryForObject(
                "select exploitation_id from animal where id = ?", UUID.class, animal))
        .isEqualTo(destination);
    entites.clear();

    mvc.perform(get("/evenements/{id}", evenement).with(utilisateur(2)))
        .andExpect(status().isNotFound());
    mvc.perform(get("/evenements/{id}", evenement).with(utilisateur(1)))
        .andExpect(status().isOk())
        .andExpect(
            jsonPath("$.details.exploitationOrigineId")
                .value("30000000-0000-4000-8000-000000000001"))
        .andExpect(jsonPath("$.details.exploitationDestinationId").value(destination.toString()));
  }

  private ResultActions annuler(int auteur, UUID id, String motif) throws Exception {
    return mvc.perform(
        patch("/evenements/{id}/annulation", id)
            .with(utilisateur(auteur))
            .contentType(MediaType.APPLICATION_JSON)
            .content(
                """
                {"motif":"%s"}
                """
                    .formatted(motif)));
  }
}
