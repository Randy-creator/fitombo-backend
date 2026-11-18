package com.fitombo.app.controller.profil;

import com.fitombo.app.controller.UtilisateurConnecte;
import com.fitombo.app.dto.profil.ModifierProfilRequest;
import com.fitombo.app.dto.profil.ProfilDto;
import com.fitombo.app.service.profil.ProfilService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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
  public ProfilDto consulter(@AuthenticationPrincipal Jwt jwt) {
    return profils.consulter(UtilisateurConnecte.id(jwt));
  }

  @PutMapping
  public ProfilDto modifier(
      @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody ModifierProfilRequest demande) {
    return profils.modifier(UtilisateurConnecte.id(jwt), demande);
  }
}
