package com.fitombo.app.service.inscription;

import com.fitombo.app.config.security.inscription.InscriptionProperties;
import com.fitombo.app.dto.inscription.DemandeSmsDto;
import com.fitombo.app.dto.inscription.VerificationSms;
import com.fitombo.app.exception.CodeErreur;
import com.fitombo.app.exception.ExceptionApplicative;
import com.fitombo.app.repository.inscription.InscriptionRepository;
import com.fitombo.app.service.connexion.LimiteTentativesConnexion;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
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
  private final CodeSms codes;
  private final InscriptionProperties configuration;
  private final ObjectProvider<TransportSms> transport;
  private final PasswordEncoder motsDePasse;

  @Transactional
  public DemandeSmsDto demander(String telephone, String adresse) {
    tentatives.enregistrer(adresse);
    inscriptions.verrouiller(telephone);
    Instant maintenant = Instant.now();
    if (inscriptions.compteExiste(telephone)) {
      throw new ExceptionApplicative(CodeErreur.COMPTE_DEJA_INSCRIT);
    }
    inscriptions
        .dernier(telephone)
        .filter(code -> code.cree().plus(configuration.delaiRenvoi()).isAfter(maintenant))
        .ifPresent(code -> {
          throw new ExceptionApplicative(CodeErreur.LIMITE_SMS_ATTEINTE);
        });
    TransportSms expediteur = transport.getIfAvailable();
    if (expediteur == null) {
      throw new ExceptionApplicative(CodeErreur.SMS_INDISPONIBLE);
    }
    String code = codes.generer();
    var id =
        inscriptions.enregistrer(
            telephone,
            codes.hacher(telephone, code),
            maintenant,
            maintenant.plus(configuration.dureeCode()));
    expediteur.envoyer(telephone, code);
    return new DemandeSmsDto(id, configuration.dureeCode().toSeconds());
  }

  @Transactional
  public boolean verifier(VerificationSms demande, String adresse) {
    tentatives.enregistrer(adresse);
    inscriptions.verrouiller(demande.telephone());
    Instant maintenant = Instant.now();
    var code =
        inscriptions
            .dernier(demande.telephone())
            .filter(candidat -> candidat.id().equals(demande.demandeId()))
            .filter(candidat ->
                !candidat.utilise()
                    && candidat.expire().isAfter(maintenant)
                    && candidat.tentatives() < configuration.maxTentatives());
    if (code.isEmpty()) {
      return false;
    }
    if (!codes.correspond(demande.telephone(), demande.code(), code.orElseThrow().hash())) {
      inscriptions.noterEchec(demande.demandeId());
      return false;
    }
    creerCompte(demande);
    inscriptions.consommer(demande.demandeId(), maintenant);
    return true;
  }

  private void creerCompte(VerificationSms demande) {
    if (demande.motDePasse().getBytes(StandardCharsets.UTF_8).length > 72) {
      throw new ExceptionApplicative(CodeErreur.SMS_INVALIDE);
    }
    String email = demande.email() == null || demande.email().isBlank() ? null : demande.email();
    if (!inscriptions.creerCompte(
        demande.nom().strip(),
        demande.telephone(),
        email,
        demande.langue(),
        motsDePasse.encode(demande.motDePasse()))) {
      throw new ExceptionApplicative(CodeErreur.COMPTE_DEJA_INSCRIT);
    }
  }
}
