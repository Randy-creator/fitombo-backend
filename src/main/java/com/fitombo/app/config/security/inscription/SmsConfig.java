package com.fitombo.app.config.security.inscription;

import com.fitombo.app.exception.CodeErreur;
import com.fitombo.app.exception.ExceptionApplicative;
import com.fitombo.app.service.inscription.TransportSms;
import java.net.http.HttpClient;
import java.time.Duration;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Configuration(proxyBeanMethods = false)
class SmsConfig {
  @Bean
  @Profile("dev & !prod")
  @ConditionalOnProperty(name = "app.sms.mode", havingValue = "console")
  TransportSms console() {
    var journal = LoggerFactory.getLogger(SmsConfig.class);
    return (telephone, code) -> journal.info("Code SMS local : {}", code);
  }

  @Bean
  @ConditionalOnProperty(name = "app.sms.mode", havingValue = "twilio")
  TransportSms twilio(
      @Value("${app.sms.twilio.account-sid}") String sid,
      @Value("${app.sms.twilio.auth-token}") String secret,
      @Value("${app.sms.twilio.from}") String expediteur) {
    if (!sid.matches("AC[0-9a-fA-F]{32}") || secret.isBlank() || expediteur.isBlank()) {
      throw new IllegalArgumentException("Configuration SMS Twilio incomplète");
    }
    var http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(4)).build();
    var factory = new JdkClientHttpRequestFactory(http);
    factory.setReadTimeout(Duration.ofSeconds(5));
    var client =
        RestClient.builder()
            .baseUrl("https://api.twilio.com")
            .requestFactory(factory)
            .defaultHeaders(headers -> headers.setBasicAuth(sid, secret))
            .build();
    return (telephone, code) -> envoyer(client, sid, expediteur, telephone, code);
  }

  private void envoyer(RestClient client, String sid, String exp, String telephone, String code) {
    var formulaire = new LinkedMultiValueMap<String, String>();
    formulaire.add("To", telephone);
    formulaire.add("From", exp);
    formulaire.add("Body", "Votre code Fitombo : " + code);
    try {
      client
          .post()
          .uri("/2010-04-01/Accounts/{sid}/Messages.json", sid)
          .contentType(MediaType.APPLICATION_FORM_URLENCODED)
          .body(formulaire)
          .retrieve()
          .toBodilessEntity();
    } catch (RestClientException exception) {
      throw new ExceptionApplicative(CodeErreur.SMS_INDISPONIBLE);
    }
  }
}
