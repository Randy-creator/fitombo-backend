package com.fitombo.app.service.connexion;

import com.fitombo.app.dto.connexion.ConnexionResponse;
import com.fitombo.app.exception.CodeErreur;
import com.fitombo.app.exception.ExceptionApplicative;
import com.fitombo.app.repository.connexion.UtilisateurRepository;
import com.fitombo.app.repository.connexion.UtilisateurRepository.Identifiants;
import com.fitombo.app.service.session.SessionService;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class ConnexionService {

  private static final int LIMITE_OCTETS_BCRYPT = 72;

  private final UtilisateurRepository utilisateurs;
  private final PasswordEncoder motsDePasse;
  private final SessionService sessions;
  private final LimiteTentativesConnexion tentatives;
  private final String hashFactice;

  ConnexionService(
      UtilisateurRepository utilisateurs,
      PasswordEncoder motsDePasse,
      SessionService sessions,
      LimiteTentativesConnexion tentatives) {
    this.utilisateurs = utilisateurs;
    this.motsDePasse = motsDePasse;
    this.sessions = sessions;
    this.tentatives = tentatives;
    this.hashFactice = motsDePasse.encode(UUID.randomUUID().toString());
  }

  public ConnexionResponse connecter(
      String telephone, String email, String motDePasse, String adresse) {
    tentatives.enregistrer(adresse);
    Identifiants compte = authentifier(telephone, email, motDePasse);
    return sessions.ouvrir(compte.getId(), compte.getRole());
  }

  private Identifiants authentifier(String telephone, String email, String motDePasse) {
    if (motDePasse.getBytes(StandardCharsets.UTF_8).length > LIMITE_OCTETS_BCRYPT) {
      throw new ExceptionApplicative(CodeErreur.IDENTIFIANTS_INVALIDES);
    }

    var compte =
        utilisateurs.findIdentifiants(
            telephone, email == null ? null : email.toLowerCase(Locale.ROOT));
    String hash = compte.map(Identifiants::getMotDePasseHash).orElse(hashFactice);
    if (!motsDePasse.matches(motDePasse, hash) || compte.isEmpty()) {
      throw new ExceptionApplicative(CodeErreur.IDENTIFIANTS_INVALIDES);
    }
    return compte.orElseThrow();
  }
}
