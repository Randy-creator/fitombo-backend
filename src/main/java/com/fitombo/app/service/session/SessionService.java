package com.fitombo.app.service.session;

import com.fitombo.app.config.security.JwtProperties;
import com.fitombo.app.dto.connexion.ConnexionResponse;
import com.fitombo.app.model.utilisateur.Role;
import com.fitombo.app.repository.session.SessionRepository;
import com.fitombo.app.repository.session.SessionRepository.Session;
import com.fitombo.app.service.connexion.LimiteTentativesConnexion;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SessionService {
  private final SessionRepository sessions;
  private final JetonAccesService jetons;
  private final JwtProperties configuration;
  private final LimiteTentativesConnexion tentatives;

  @Transactional
  public ConnexionResponse ouvrir(UUID utilisateurId, Role role) {
    String jeton = JetonRafraichissement.generer();
    sessions.creer(
        utilisateurId,
        JetonRafraichissement.hacher(jeton),
        Instant.now().plus(configuration.refreshTokenTtl()));
    return jetons.emettre(utilisateurId, role, jeton);
  }

  @Transactional
  public Optional<ConnexionResponse> renouveler(String jeton, String adresse) {
    tentatives.enregistrer(adresse);
    String hash = JetonRafraichissement.hacher(jeton);
    return chargerSousVerrou(hash).flatMap(this::faireRotation);
  }

  private Optional<Session> chargerSousVerrou(String hash) {
    var session = sessions.chercher(hash);
    if (session.isEmpty()) {
      return Optional.empty();
    }
    sessions.verrouiller(session.orElseThrow().utilisateurId());
    return sessions.chercher(hash);
  }

  private Optional<ConnexionResponse> faireRotation(Session session) {
    if (session.revoquee()) {
      sessions.revoquerChaine(session.id());
      return Optional.empty();
    }
    if (!session.compteValide() || !session.expiration().isAfter(Instant.now())) {
      return Optional.empty();
    }

    String nouveau = JetonRafraichissement.generer();
    UUID suivant =
        sessions.creer(
            session.utilisateurId(), JetonRafraichissement.hacher(nouveau), session.expiration());
    sessions.remplacer(session.id(), suivant);
    return Optional.of(jetons.emettre(session.utilisateurId(), session.role(), nouveau));
  }
}
