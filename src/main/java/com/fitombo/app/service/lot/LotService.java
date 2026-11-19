package com.fitombo.app.service.lot;

import com.fitombo.app.dto.PageResult;
import com.fitombo.app.dto.lot.CreerLotRequest;
import com.fitombo.app.dto.lot.LotDto;
import com.fitombo.app.exception.CodeErreur;
import com.fitombo.app.exception.ExceptionApplicative;
import com.fitombo.app.model.cheptel.Lot;
import com.fitombo.app.model.cheptel.StatutLot;
import com.fitombo.app.model.exploitation.Emplacement;
import com.fitombo.app.model.referentiel.Espece;
import com.fitombo.app.model.referentiel.ModeSuivi;
import com.fitombo.app.model.referentiel.Race;
import com.fitombo.app.repository.ConflitsPersistance;
import com.fitombo.app.repository.EspeceRepository;
import com.fitombo.app.repository.RaceRepository;
import com.fitombo.app.repository.emplacement.EmplacementRepository;
import com.fitombo.app.repository.lot.LotRepository;
import com.fitombo.app.service.ExploitationService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
public class LotService {
  private final ExploitationService exploitations;
  private final LotRepository lots;
  private final EspeceRepository especes;
  private final RaceRepository races;
  private final EmplacementRepository emplacements;

  public PageResult<LotDto> lister(
      UUID utilisateurId, UUID exploitationId, StatutLot statut, Pageable pageable) {
    exploitations.verifierAcces(utilisateurId, exploitationId);
    return PageResult.from(lots.findVisibles(exploitationId, statut, pageable));
  }

  @Transactional(isolation = Isolation.REPEATABLE_READ)
  public LotDto creer(UUID utilisateurId, UUID exploitationId, CreerLotRequest demande) {
    var exploitation = exploitations.chargerPourGestion(utilisateurId, exploitationId);
    var espece = chargerEspecePourLot(demande.especeCode());
    var race = chargerRace(demande.raceId(), espece.getCode());
    var emplacement = chargerEmplacement(demande.emplacementId(), exploitationId);
    var lot =
        new Lot(
            exploitation,
            espece,
            race,
            emplacement,
            demande.code(),
            demande.dateCreation(),
            demande.effectifInitial());
    ConflitsPersistance.traduire(
        () -> lots.saveAndFlush(lot), "uq_lot_code", CodeErreur.LOT_CODE_UTILISE);
    return new LotDto(
        lot.getId(),
        lot.getCode(),
        espece.getCode(),
        lot.getDateCreation(),
        lot.getEffectifInitial(),
        lot.getEffectifActuel(),
        lot.getStatut());
  }

  private Espece chargerEspecePourLot(String code) {
    Espece espece =
        especes
            .findByCodeAndActifTrue(code)
            .orElseThrow(() -> new ExceptionApplicative(CodeErreur.ESPECE_INTROUVABLE));
    if (espece.getModeSuivi() == ModeSuivi.INDIVIDUEL) {
      throw new ExceptionApplicative(CodeErreur.ESPECE_NON_SUIVIE_EN_LOT);
    }
    return espece;
  }

  private Race chargerRace(UUID id, String especeCode) {
    if (id == null) {
      return null;
    }
    return races
        .findByIdAndEspeceCodeAndActifTrue(id, especeCode)
        .orElseThrow(() -> new ExceptionApplicative(CodeErreur.RACE_INCOMPATIBLE));
  }

  private Emplacement chargerEmplacement(UUID id, UUID exploitationId) {
    if (id == null) {
      return null;
    }
    return emplacements
        .findByIdAndExploitationIdAndDeletedAtIsNull(id, exploitationId)
        .orElseThrow(() -> new ExceptionApplicative(CodeErreur.EMPLACEMENT_INTROUVABLE));
  }
}
