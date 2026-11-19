package com.fitombo.app.config.security.inscription;

import com.fitombo.app.exception.CodeErreur;
import com.fitombo.app.exception.ExceptionApplicative;
import com.fitombo.app.service.inscription.TransportEmail;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Configuration(proxyBeanMethods = false)
class EmailConfig {
  @Bean
  @Profile("dev & !prod")
  @ConditionalOnProperty(name = "app.email.mode", havingValue = "console")
  TransportEmail console() {
    var journal = LoggerFactory.getLogger(EmailConfig.class);
    return (email, code) -> journal.info("Code e-mail local : {}", code);
  }

  @Bean
  @ConditionalOnProperty(name = "app.email.mode", havingValue = "brevo")
  TransportEmail brevo(
      @Value("${app.email.brevo.api-key}") String cle, @Value("${app.email.from}") String adresse) {
    if (cle.isBlank() || adresse.isBlank()) {
      throw new IllegalArgumentException("BREVO_API_KEY et EMAIL_FROM sont requis");
    }
    var http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(4)).build();
    var requetes = new JdkClientHttpRequestFactory(http);
    requetes.setReadTimeout(Duration.ofSeconds(5));
    var client =
        RestClient.builder()
            .baseUrl("https://api.brevo.com")
            .requestFactory(requetes)
            .defaultHeader("api-key", cle)
            .build();
    return (email, code) -> envoyer(client, adresse, email, code);
  }

  private void envoyer(RestClient client, String expediteur, String destinataire, String code) {
    var message =
        Map.of(
            "sender",
            Map.of("email", expediteur, "name", "Fitombo"),
            "to",
            List.of(Map.of("email", destinataire)),
            "subject",
            "Code d’inscription Fitombo",
            "textContent",
            "Votre code Fitombo : " + code);
    try {
      client
          .post()
          .uri("/v3/smtp/email")
          .contentType(MediaType.APPLICATION_JSON)
          .body(message)
          .retrieve()
          .toBodilessEntity();
    } catch (RestClientException exception) {
      throw new ExceptionApplicative(CodeErreur.EMAIL_INDISPONIBLE);
    }
  }
}
