package com.fitombo.app.service.exportation;

import com.fitombo.app.dto.exportation.PeriodeExport;
import com.fitombo.app.exception.CodeErreur;
import com.fitombo.app.exception.ExceptionApplicative;
import com.fitombo.app.repository.exportation.ExportRepository;
import com.fitombo.app.service.animal.AnimalService;
import com.fitombo.app.service.exploitation.ExploitationService;
import com.fitombo.app.service.lot.LotService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
public class ExportService {
  private static final int LIMITE = 10_000;
  private final AnimalService animaux;
  private final LotService lots;
  private final ExploitationService exploitations;
  private final ExportRepository donnees;
  private final RegistrePdf pdf;
  private final ClasseurExploitation excel;

  public byte[] registreAnimal(UUID utilisateur, UUID id, PeriodeExport periode) {
    var animal = animaux.consulter(utilisateur, id);
    return pdf.ecrire(
        "Animal " + animal.identifiant(),
        animal.especeCode(),
        periode,
        borner(donnees.evenements(null, id, null, periode, LIMITE + 1)));
  }

  public byte[] registreLot(UUID utilisateur, UUID id, PeriodeExport periode) {
    var lot = lots.consulter(utilisateur, id);
    return pdf.ecrire(
        "Lot " + lot.code(),
        lot.especeCode(),
        periode,
        borner(donnees.evenements(null, null, id, periode, LIMITE + 1)));
  }

  public byte[] classeur(UUID utilisateur, UUID exploitation, PeriodeExport periode) {
    exploitations.chargerPourGestion(utilisateur, exploitation);
    return excel.ecrire(
        borner(donnees.evenements(exploitation, null, null, periode, LIMITE + 1)),
        borner(donnees.transactions(exploitation, periode, LIMITE + 1)));
  }

  private <T> List<T> borner(List<T> lignes) {
    if (lignes.size() > LIMITE) {
      throw new ExceptionApplicative(CodeErreur.EXPORT_TROP_VOLUMINEUX);
    }
    return lignes;
  }
}
