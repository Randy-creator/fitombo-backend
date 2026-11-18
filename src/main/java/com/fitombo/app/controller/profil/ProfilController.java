package com.fitombo.app.controller.profil;

import com.fitombo.app.controller.UtilisateurCourant;
import com.fitombo.app.dto.profil.ModifierProfilRequest;
import com.fitombo.app.dto.profil.ProfilDto;
import com.fitombo.app.service.profil.ProfilService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/me")
@RequiredArgsConstructor
public class ProfilController {
  private final ProfilService profils;

  @GetMapping
  public ProfilDto consulter(@UtilisateurCourant UUID utilisateurId) {
    return profils.consulter(utilisateurId);
  }

  @PutMapping
  public ProfilDto modifier(
      @UtilisateurCourant UUID utilisateurId, @Valid @RequestBody ModifierProfilRequest demande) {
    return profils.modifier(utilisateurId, demande);
  }
}
