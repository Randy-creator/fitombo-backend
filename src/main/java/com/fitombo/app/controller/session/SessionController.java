package com.fitombo.app.controller.session;

import com.fitombo.app.dto.connexion.ConnexionResponse;
import com.fitombo.app.dto.session.SessionRequest;
import com.fitombo.app.exception.CodeErreur;
import com.fitombo.app.exception.ExceptionApplicative;
import com.fitombo.app.service.session.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SessionController {
  private final SessionService sessions;

  @PostMapping("/auth/refresh")
  public ResponseEntity<ConnexionResponse> renouveler(
      @Valid @RequestBody SessionRequest demande, HttpServletRequest requete) {
    var resultat =
        sessions
            .renouveler(demande.refreshToken(), requete.getRemoteAddr())
            .orElseThrow(() -> new ExceptionApplicative(CodeErreur.SESSION_INVALIDE));
    return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(resultat);
  }
}
