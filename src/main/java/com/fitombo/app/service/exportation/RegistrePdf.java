package com.fitombo.app.service.exportation;

import com.fitombo.app.dto.exportation.PeriodeExport;
import com.fitombo.app.repository.exportation.ExportRepository.EvenementExport;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Objects;
import org.openpdf.text.Document;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;

@Component
class RegistrePdf {
  byte[] ecrire(String cible, String espece, PeriodeExport periode, List<EvenementExport> lignes) {
    var sortie = new ByteArrayOutputStream();
    var document = new Document(PageSize.A4, 40, 40, 42, 42);
    PdfWriter.getInstance(document, sortie);
    document.addTitle("Registre d’élevage Fitombo");
    document.open();
    try {
      document.add(new Paragraph("Registre d’élevage", new Font(Font.HELVETICA, 22, Font.BOLD)));
      document.add(new Paragraph(cible + "  |  " + espece));
      if (periode.du() != null || periode.au() != null) {
        document.add(
            new Paragraph(
                "Période : "
                    + Objects.toString(periode.du(), "début")
                    + " – "
                    + Objects.toString(periode.au(), "aujourd’hui")));
      }
      if (lignes.isEmpty()) {
        document.add(new Paragraph("Aucun événement pour cette période."));
      } else {
        document.add(tableau(lignes));
      }
    } finally {
      document.close();
    }
    return sortie.toByteArray();
  }

  private PdfPTable tableau(List<EvenementExport> lignes) {
    var tableau = new PdfPTable(new float[] {20, 23, 57});
    tableau.setWidthPercentage(100);
    tableau.setSpacingBefore(18);
    for (String titre : List.of("Date", "Type et statut", "Détails et notes")) {
      ajouter(tableau, titre, true);
    }
    tableau.setHeaderRows(1);
    for (var ligne : lignes) {
      ajouter(tableau, ligne.date().toString(), false);
      ajouter(tableau, ligne.type().replace('_', ' ') + "\n" + ligne.statut(), false);
      String details = Objects.toString(ligne.details(), "");
      ajouter(tableau, ligne.notes() == null ? details : details + "\n" + ligne.notes(), false);
    }
    return tableau;
  }

  private void ajouter(PdfPTable tableau, String texte, boolean entete) {
    var police =
        new Font(
            Font.HELVETICA,
            9,
            entete ? Font.BOLD : Font.NORMAL,
            entete ? Color.WHITE : Color.DARK_GRAY);
    var cellule = new PdfPCell(new Phrase(texte, police));
    cellule.setVerticalAlignment(Element.ALIGN_TOP);
    cellule.setPadding(7);
    cellule.setBorderColor(new Color(218, 227, 225));
    if (entete) {
      cellule.setBackgroundColor(new Color(29, 83, 73));
    }
    tableau.addCell(cellule);
  }
}
