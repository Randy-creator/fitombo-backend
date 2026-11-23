package com.fitombo.app.config.security;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("app.security.jwt")
public record JwtProperties(
    @NotBlank String secret,
    @NotBlank String issuer,
    @NotNull Duration accessTokenTtl,
    @NotNull Duration refreshTokenTtl) {

  public JwtProperties {
    if (refreshTokenTtl != null && (refreshTokenTtl.isZero() || refreshTokenTtl.isNegative())) {
      throw new IllegalArgumentException(
          "La durée du jeton de rafraîchissement doit être positive");
    }
    if (accessTokenTtl != null
        && (accessTokenTtl.compareTo(Duration.ofSeconds(1)) < 0
            || accessTokenTtl.compareTo(Duration.ofMinutes(30)) > 0)) {
      throw new IllegalArgumentException(
          "La durée du JWT d’accès doit être comprise entre 1 seconde et 30 minutes");
    }
  }

  @Override
  public String toString() {
    return "JwtProperties[issuer=" + issuer + ", accessTokenTtl=" + accessTokenTtl + "]";
  }
}
