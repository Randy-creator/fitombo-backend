package com.fitombo.app.service.evenement;

import com.fitombo.app.dto.alimentation.CreerAlimentationRequest;
import com.fitombo.app.dto.deces.CreerDecesRequest;
import com.fitombo.app.dto.evenement.EvenementSaisiDto;
import com.fitombo.app.dto.evenement.SaisieEvenement;
import com.fitombo.app.dto.production.CreerProductionRequest;
import com.fitombo.app.dto.traitement.CreerTraitementRequest;
import com.fitombo.app.dto.vaccination.CreerVaccinationRequest;
import com.fitombo.app.model.evenement.Alimentation;
import com.fitombo.app.model.evenement.Evenement;
import com.fitombo.app.model.evenement.Production;
import com.fitombo.app.model.evenement.Traitement;
import com.fitombo.app.model.evenement.TypeEvenement;
import com.fitombo.app.model.evenement.Vaccination;
import com.fitombo.app.repository.evenement.EvenementRepository;
import com.fitombo.app.service.deces.DecesService;
import com.fitombo.app.service.evenement.CiblesEvenement.Cible;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SaisieEvenementService {
  private final CiblesEvenement cibles;
  private final DecesService deces;
  private final EvenementRepository evenements;

  @Transactional
  public EvenementSaisiDto enregistrer(UUID utilisateurId, SaisieEvenement demande) {
    return enregistrer(cibles.charger(utilisateurId, demande), demande, null);
  }

  public EvenementSaisiDto enregistrer(Cible cible, SaisieEvenement demande, Evenement original) {
    Evenement evenement = creer(cible, demande);
    if (original != null) {
      evenement.remplacer(original);
    }
    evenements.saveAndFlush(evenement);
    return new EvenementSaisiDto(
        evenement.getId(),
        TypeEvenement.valueOf(demande.type()),
        demande.animalId(),
        demande.lotId(),
        demande.dateEvenement());
  }

  private Evenement creer(Cible cible, SaisieEvenement demande) {
    return switch (demande) {
      case CreerAlimentationRequest demandeSpecialisee ->
          new Alimentation(
              cible.exploitation(),
              cible.animal(),
              cible.lot(),
              demande.dateEvenement(),
              cible.auteur(),
              demande.notes(),
              demandeSpecialisee.aliment(),
              demandeSpecialisee.quantiteKg(),
              demandeSpecialisee.coutAriary());
      case CreerVaccinationRequest demandeSpecialisee ->
          new Vaccination(
              cible.exploitation(),
              cible.animal(),
              cible.lot(),
              demande.dateEvenement(),
              cible.auteur(),
              demande.notes(),
              demandeSpecialisee.vaccin(),
              demandeSpecialisee.maladieCiblee(),
              demandeSpecialisee.lotVaccin(),
              demandeSpecialisee.rappelPrevu());
      case CreerProductionRequest demandeSpecialisee ->
          new Production(
              cible.exploitation(),
              cible.animal(),
              cible.lot(),
              demande.dateEvenement(),
              cible.auteur(),
              demande.notes(),
              demandeSpecialisee.typeProduction(),
              demandeSpecialisee.quantite(),
              demandeSpecialisee.unite());
      case CreerTraitementRequest demandeSpecialisee ->
          new Traitement(
              cible.exploitation(),
              cible.animal(),
              cible.lot(),
              demande.dateEvenement(),
              cible.auteur(),
              demande.notes(),
              demandeSpecialisee.produit(),
              demandeSpecialisee.maladie(),
              demandeSpecialisee.dose(),
              demandeSpecialisee.dureeJours(),
              demandeSpecialisee.delaiAttenteJours());
      case CreerDecesRequest saisie -> deces.creer(cible, saisie);
      default -> throw new IllegalArgumentException("Type d’événement inconnu");
    };
  }
}
