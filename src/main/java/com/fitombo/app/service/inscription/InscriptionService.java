package com.fitombo.app.service.inscription;

import com.fitombo.app.config.security.inscription.InscriptionProperties;
import com.fitombo.app.dto.inscription.DemandeVerificationDto;
import com.fitombo.app.dto.inscription.VerificationEmail;
import com.fitombo.app.exception.CodeErreur;
import com.fitombo.app.exception.ExceptionApplicative;
import com.fitombo.app.repository.inscription.InscriptionRepository;
import com.fitombo.app.service.connexion.LimiteTentativesConnexion;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InscriptionService {
  private final InscriptionRepository inscriptions;
  private final LimiteTentativesConnexion tentatives;
  private final CodeInscription codes;
  private final InscriptionProperties configuration;
  private final ObjectProvider<TransportEmail> transport;
  private final PasswordEncoder motsDePasse;

  @Transactional
  public DemandeVerificationDto demander(String email, String adresse) {
    tentatives.enregistrer(adresse);
    String destinataire = email.toLowerCase(Locale.ROOT);
    inscriptions.verrouiller(destinataire);
    Instant maintenant = Instant.now();
    if (inscriptions.compteExiste(destinataire)) {
      throw new ExceptionApplicative(CodeErreur.COMPTE_DEJA_INSCRIT);
    }
    inscriptions
        .dernier(destinataire)
        .filter(code -> code.cree().plus(configuration.delaiRenvoi()).isAfter(maintenant))
        .ifPresent(code -> {
          throw new ExceptionApplicative(CodeErreur.LIMITE_EMAIL_ATTEINTE);
        });
    TransportEmail expediteur = transport.getIfAvailable();
    if (expediteur == null) {
      throw new ExceptionApplicative(CodeErreur.EMAIL_INDISPONIBLE);
    }
    String code = codes.generer();
    var id =
        inscriptions.enregistrer(
            destinataire,
            codes.hacher(destinataire, code),
            maintenant,
            maintenant.plus(configuration.dureeCode()));
    expediteur.envoyer(destinataire, code);
    return new DemandeVerificationDto(id, configuration.dureeCode().toSeconds());
  }

  @Transactional
  public boolean verifier(VerificationEmail demande, String adresse) {
    tentatives.enregistrer(adresse);
    String destinataire = demande.email().toLowerCase(Locale.ROOT);
    inscriptions.verrouiller(destinataire);
    Instant maintenant = Instant.now();
    var code =
        inscriptions
            .dernier(destinataire)
            .filter(candidat -> candidat.id().equals(demande.demandeId()))
            .filter(candidat ->
                !candidat.utilise()
                    && candidat.expire().isAfter(maintenant)
                    && candidat.tentatives() < configuration.maxTentatives());
    if (code.isEmpty()) {
      return false;
    }
    if (!codes.correspond(destinataire, demande.code(), code.orElseThrow().hash())) {
      inscriptions.noterEchec(demande.demandeId());
      return false;
    }
    creerCompte(demande, destinataire);
    inscriptions.consommer(demande.demandeId(), maintenant);
    return true;
  }

  private void creerCompte(VerificationEmail demande, String destinataire) {
    if (demande.motDePasse().getBytes(StandardCharsets.UTF_8).length > 72) {
      throw new ExceptionApplicative(CodeErreur.CODE_EMAIL_INVALIDE);
    }
    if (!inscriptions.creerCompte(
        demande.nom().strip(),
        destinataire,
        demande.langue(),
        motsDePasse.encode(demande.motDePasse()))) {
      throw new ExceptionApplicative(CodeErreur.COMPTE_DEJA_INSCRIT);
    }
  }
}
