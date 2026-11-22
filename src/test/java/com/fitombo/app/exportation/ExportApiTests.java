package com.fitombo.app.exportation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fitombo.app.ApiIntegrationTest;
import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.UUID;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.openpdf.text.pdf.PdfReader;
import org.openpdf.text.pdf.parser.PdfTextExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

@Sql("/sql/exploitations-api.sql")
class ExportApiTests extends ApiIntegrationTest {
  private static final UUID FERME = UUID.fromString("30000000-0000-4000-8000-000000000001");
  @Autowired private JdbcTemplate jdbc;

  @Test
  void exporteLeRegistrePourUnMembreAutorise() throws Exception {
    UUID animal = animalActif();
    enregistrerPesee(animal);
    mvc.perform(get("/animaux/{id}/registre.pdf", animal).with(utilisateur(4)))
        .andExpect(status().isNotFound());
    byte[] contenu =
        mvc.perform(get("/animaux/{id}/registre.pdf", animal).with(utilisateur(2)))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Type", "application/pdf"))
            .andExpect(header().string("Cache-Control", "no-store"))
            .andReturn()
            .getResponse()
            .getContentAsByteArray();
    var document = new PdfReader(contenu);
    try {
      assertThat(new PdfTextExtractor(document).getTextFromPage(1))
          .contains("Registre d’élevage", "API-ACTIF", "PESEE", "125.500 kg");
    } finally {
      document.close();
    }
    UUID lot =
        jdbc.queryForObject(
            """
            insert into lot(exploitation_id, espece_code, code, effectif_initial, effectif_actuel)
            values (?, 'PORC', 'LOT-PDF', 10, 10) returning id
            """,
            UUID.class,
            FERME);
    mvc.perform(get("/lots/{id}/registre.pdf", lot).with(utilisateur(2)))
        .andExpect(status().isOk())
        .andExpect(header().string("Content-Type", "application/pdf"));
    mvc.perform(get("/lots/{id}/registre.pdf", lot).with(utilisateur(4)))
        .andExpect(status().isNotFound());
  }

  @Test
  void exporteLesDonneesDuProprietaireAvecMontantsNumeriquesEtNotesTextuelles() throws Exception {
    enregistrerPesee(animalActif());
    jdbc.update(
        """
        insert into transaction_financiere(exploitation_id, type, categorie, montant_ariary, date_transaction, notes)
        values (?, 'RECETTE', 'VENTE_LAIT', 125.50, '2026-06-01', '=1+1'),
               (?, 'RECETTE', 'VENTE_LAIT', 300, '2026-07-01', null)
        """,
        FERME,
        FERME);
    String url = "/exploitations/{id}/export.xlsx";
    mvc.perform(get(url, FERME).with(utilisateur(2))).andExpect(status().isNotFound());
    mvc.perform(
            get(url, FERME)
                .with(utilisateur(1))
                .param("du", "2026-07-01")
                .param("au", "2026-06-01"))
        .andExpect(status().isBadRequest());
    byte[] contenu =
        mvc.perform(
                get(url, FERME)
                    .with(utilisateur(1))
                    .param("du", "2026-06-01")
                    .param("au", "2026-06-30"))
            .andExpect(status().isOk())
            .andExpect(
                header()
                    .string(
                        "Content-Disposition",
                        "attachment; filename=\"exploitation-" + FERME + ".xlsx\""))
            .andReturn()
            .getResponse()
            .getContentAsByteArray();
    try (var classeur = new XSSFWorkbook(new ByteArrayInputStream(contenu))) {
      assertThat(classeur.getSheet("Événements").getRow(1).getCell(1).getStringCellValue())
          .isEqualTo("PESEE");
      var finances = classeur.getSheet("Finances");
      assertThat(finances.getLastRowNum()).isEqualTo(1);
      var ligne = finances.getRow(1);
      assertThat(ligne.getCell(0).getLocalDateTimeCellValue().toLocalDate())
          .isEqualTo(LocalDate.of(2026, 6, 1));
      assertThat(ligne.getCell(3).getNumericCellValue()).isEqualTo(125.5);
      assertThat(ligne.getCell(7).getCellType()).isEqualTo(CellType.STRING);
      assertThat(ligne.getCell(7).getStringCellValue()).isEqualTo("=1+1");
    }
  }

  private UUID animalActif() {
    return jdbc.queryForObject("select id from animal where identifiant = 'API-ACTIF'", UUID.class);
  }

  private void enregistrerPesee(UUID animal) {
    UUID id =
        jdbc.queryForObject(
            """
            insert into evenement(exploitation_id, animal_id, type, date_evenement, saisi_par)
            values (?, ?, 'PESEE', '2026-06-01', '10000000-0000-4000-8000-000000000001')
            returning id
            """,
            UUID.class,
            FERME,
            animal);
    jdbc.update(
        "insert into pesee(evenement_id, poids_kg, methode) values (?, 125.500, 'BALANCE')", id);
  }
}
