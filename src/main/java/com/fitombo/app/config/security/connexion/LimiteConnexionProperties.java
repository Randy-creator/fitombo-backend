package com.fitombo.app.config.security.connexion;

import java.time.Duration;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("app.security.login")
public record LimiteConnexionProperties(
        @Positive int maxAttemptsPerAddress,
        @Positive int maxAddressesPerWindow,
        @NotNull Duration window
) {
    public LimiteConnexionProperties {
        if (window != null && (window.isZero() || window.isNegative())) {
            throw new IllegalArgumentException("La fenêtre de limitation doit être positive");
        }
    }
}
