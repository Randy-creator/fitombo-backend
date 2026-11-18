package com.fitombo.app.config.security;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("app.security.jwt")
record JwtProperties(@NotBlank String secret, @NotBlank String issuer) {
}
