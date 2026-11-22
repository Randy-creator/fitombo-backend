package com.fitombo.app.controller.exportation;

import com.fitombo.app.controller.UtilisateurCourant;
import com.fitombo.app.dto.exportation.PeriodeExport;
import com.fitombo.app.service.exportation.ExportService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ExportController {
  private static final String XLSX =
      "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
  private final ExportService exports;

  @GetMapping(value = "/animaux/{id}/registre.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
  public ResponseEntity<byte[]> registreAnimal(
      @UtilisateurCourant UUID utilisateurId,
      @PathVariable UUID id,
      @Valid @ModelAttribute PeriodeExport periode) {
    return telecharger(
        exports.registreAnimal(utilisateurId, id, periode),
        "registre-animal-" + id + ".pdf",
        MediaType.APPLICATION_PDF);
  }

  @GetMapping(value = "/lots/{id}/registre.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
  public ResponseEntity<byte[]> registreLot(
      @UtilisateurCourant UUID utilisateurId,
      @PathVariable UUID id,
      @Valid @ModelAttribute PeriodeExport periode) {
    return telecharger(
        exports.registreLot(utilisateurId, id, periode),
        "registre-lot-" + id + ".pdf",
        MediaType.APPLICATION_PDF);
  }

  @GetMapping(value = "/exploitations/{id}/export.xlsx", produces = XLSX)
  public ResponseEntity<byte[]> classeur(
      @UtilisateurCourant UUID utilisateurId,
      @PathVariable UUID id,
      @Valid @ModelAttribute PeriodeExport periode) {
    return telecharger(
        exports.classeur(utilisateurId, id, periode),
        "exploitation-" + id + ".xlsx",
        MediaType.parseMediaType(XLSX));
  }

  private ResponseEntity<byte[]> telecharger(byte[] fichier, String nom, MediaType type) {
    return ResponseEntity.ok()
        .contentType(type)
        .cacheControl(CacheControl.noStore())
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            ContentDisposition.attachment().filename(nom).build().toString())
        .body(fichier);
  }
}
