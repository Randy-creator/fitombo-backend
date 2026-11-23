package com.fitombo.app.service.profil;

import com.fitombo.app.dto.profil.ModifierProfilRequest;
import com.fitombo.app.dto.profil.ProfilDto;
import com.fitombo.app.exception.CodeErreur;
import com.fitombo.app.exception.ExceptionApplicative;
import com.fitombo.app.model.utilisateur.Utilisateur;
import com.fitombo.app.repository.profil.ProfilRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfilService {
  private final ProfilRepository profils;

  @Transactional(readOnly = true)
  public ProfilDto consulter(UUID id) {
    return presenter(charger(id));
  }

  @Transactional
  public ProfilDto modifier(UUID id, ModifierProfilRequest demande) {
    Utilisateur utilisateur = charger(id);
    utilisateur.modifierProfil(demande.nom(), demande.langue());
    return presenter(utilisateur);
  }

  private Utilisateur charger(UUID id) {
    return profils
        .findByIdAndActifTrueAndDeletedAtIsNull(id)
        .orElseThrow(() -> new ExceptionApplicative(CodeErreur.PROFIL_INTROUVABLE));
  }

  private ProfilDto presenter(Utilisateur utilisateur) {
    return new ProfilDto(
        utilisateur.getId(),
        utilisateur.getNom(),
        utilisateur.getTelephone(),
        utilisateur.getEmail(),
        utilisateur.getLangue(),
        utilisateur.getRole());
  }
}
