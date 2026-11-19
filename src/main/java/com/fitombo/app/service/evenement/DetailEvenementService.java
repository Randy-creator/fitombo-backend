package com.fitombo.app.service.evenement;

import com.fitombo.app.dto.evenement.EvenementDetailDto;
import com.fitombo.app.exception.CodeErreur;
import com.fitombo.app.exception.ExceptionApplicative;
import com.fitombo.app.model.commun.EntiteUuid;
import com.fitombo.app.model.evenement.Deces;
import com.fitombo.app.model.evenement.DiagnosticGestation;
import com.fitombo.app.model.evenement.Evenement;
import com.fitombo.app.model.evenement.MiseBas;
import com.fitombo.app.model.evenement.Mouvement;
import com.fitombo.app.model.evenement.Saillie;
import com.fitombo.app.repository.animal.AnimalRepository;
import com.fitombo.app.repository.evenement.EvenementRepository;
import com.fitombo.app.repository.lot.LotRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DetailEvenementService {
  private final EvenementRepository evenements;
  private final AnimalRepository animaux;
  private final LotRepository lots;

  @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
  public EvenementDetailDto consulter(UUID utilisateur, UUID id) {
    Evenement evenement =
        evenements
            .findById(id)
            .filter(e -> e.getDeletedAt() == null)
            .orElseThrow(this::introuvable);
    UUID animal = id(evenement.getAnimal());
    UUID lot = id(evenement.getLot());
    boolean visible =
        animal == null
            ? lots.existeVisible(lot, utilisateur)
            : animaux.existeVisible(animal, utilisateur);
    if (!visible) {
      throw introuvable();
    }
    return new EvenementDetailDto(
        id,
        evenement.getTypeEvenement(),
        animal,
        lot,
        evenement.getDateEvenement(),
        evenement.getNotes(),
        evenement.getStatut(),
        id(evenement.getCorrigeEvenement()),
        evenement.getMotifAnnulation(),
        details(evenement, utilisateur));
  }

  private EvenementDetailDto.Details details(Evenement evenement, UUID utilisateur) {
    return switch (evenement) {
      case Saillie s ->
          new EvenementDetailDto.Saillie(
              s.getMethode(),
              id(s.getMale()),
              s.getMaleExterne(),
              s.getReferenceSemence(),
              s.getMiseBasPrevue());
      case DiagnosticGestation d ->
          new EvenementDetailDto.Diagnostic(d.getPositif(), d.getMethode(), id(d.getSaillie()));
      case MiseBas m ->
          new EvenementDetailDto.Naissance(
              m.getNesVivants(),
              m.getMortsNes(),
              m.isAssistance(),
              id(m.getSaillie()),
              id(m.getLotPetits()),
              m.getPetits().stream().map(EntiteUuid::getId).sorted().toList());
      case Mouvement m -> detailsMouvement(m, utilisateur);
      case Deces d ->
          new EvenementDetailDto.Deces(
              d.getCause(), d.getNombre(), d.getMaladieSuspectee(), d.isDeclarationFaite());
      default -> null;
    };
  }

  private EvenementDetailDto.Mouvement detailsMouvement(Mouvement mouvement, UUID utilisateur) {
    boolean proprietaire =
        mouvement.getExploitation().getEleveur().getUtilisateur().getId().equals(utilisateur);
    return new EvenementDetailDto.Mouvement(
        mouvement.getTypeMouvement(),
        mouvement.getQuantite(),
        proprietaire ? mouvement.getPrixTotalAriary() : null,
        proprietaire ? mouvement.getModePrix() : null,
        mouvement.getPoidsTotalKg(),
        mouvement.getNumeroPieceCirculation(),
        mouvement.getTiers() == null ? null : mouvement.getTiers().getNom(),
        id(mouvement.getExploitationOrigine()),
        id(mouvement.getExploitationDestination()),
        id(mouvement.getEmplacementDestination()));
  }

  private UUID id(EntiteUuid cible) {
    return cible == null ? null : cible.getId();
  }

  private ExceptionApplicative introuvable() {
    return new ExceptionApplicative(CodeErreur.EVENEMENT_INTROUVABLE);
  }
}
