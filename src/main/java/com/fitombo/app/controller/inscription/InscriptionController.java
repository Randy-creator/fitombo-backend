package com.fitombo.app.controller.inscription;

import com.fitombo.app.dto.inscription.DemandeCode;
import com.fitombo.app.dto.inscription.DemandeSmsDto;
import com.fitombo.app.dto.inscription.VerificationSms;
import com.fitombo.app.exception.CodeErreur;
import com.fitombo.app.exception.ExceptionApplicative;
import com.fitombo.app.service.inscription.InscriptionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class InscriptionController {
  private final InscriptionService inscriptions;

  @PostMapping("/auth/inscription")
  @ResponseStatus(HttpStatus.ACCEPTED)
  public DemandeSmsDto demander(
      @Valid @RequestBody DemandeCode demande, HttpServletRequest requete) {
    return inscriptions.demander(demande.telephone(), requete.getRemoteAddr());
  }

  @PostMapping("/auth/verification-sms")
  @ResponseStatus(HttpStatus.CREATED)
  public void verifier(@Valid @RequestBody VerificationSms demande, HttpServletRequest requete) {
    if (!inscriptions.verifier(demande, requete.getRemoteAddr())) {
      throw new ExceptionApplicative(CodeErreur.SMS_INVALIDE);
    }
  }
}
