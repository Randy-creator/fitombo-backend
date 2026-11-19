package com.fitombo.app.controller.connexion;

import com.fitombo.app.controller.UtilisateurCourant;
import com.fitombo.app.dto.connexion.ConnexionRequest;
import com.fitombo.app.dto.connexion.ConnexionResponse;
import com.fitombo.app.dto.session.SessionRequest;
import com.fitombo.app.exception.CodeErreur;
import com.fitombo.app.exception.ExceptionApplicative;
import com.fitombo.app.service.connexion.ConnexionService;
import com.fitombo.app.service.session.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class ConnexionController {

  private final ConnexionService connexion;
  private final SessionService sessions;

  @PostMapping("/connexion")
  public ResponseEntity<ConnexionResponse> connecter(
      @Valid @RequestBody ConnexionRequest donnees, HttpServletRequest requete) {
    var resultat =
        connexion.connecter(
            donnees.telephone(), donnees.email(), donnees.motDePasse(), requete.getRemoteAddr());
    return reponseJetons(resultat);
  }

  @PostMapping("/refresh")
  public ResponseEntity<ConnexionResponse> renouveler(
      @Valid @RequestBody SessionRequest demande, HttpServletRequest requete) {
    var resultat =
        sessions
            .renouveler(demande.refreshToken(), requete.getRemoteAddr())
            .orElseThrow(() -> new ExceptionApplicative(CodeErreur.SESSION_INVALIDE));
    return reponseJetons(resultat);
  }

  @PostMapping("/deconnexion")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deconnecter(
      @UtilisateurCourant UUID utilisateurId, @Valid @RequestBody SessionRequest demande) {
    sessions.fermer(utilisateurId, demande.refreshToken());
  }

  private ResponseEntity<ConnexionResponse> reponseJetons(ConnexionResponse resultat) {
    return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(resultat);
  }
}
