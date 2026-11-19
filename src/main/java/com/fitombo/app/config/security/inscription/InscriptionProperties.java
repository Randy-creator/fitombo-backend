package com.fitombo.app.config.security.inscription;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.security.inscription")
public record InscriptionProperties(Duration dureeCode, Duration delaiRenvoi, int maxTentatives) {
  public InscriptionProperties {
    if (dureeCode == null
        || dureeCode.isNegative()
        || dureeCode.isZero()
        || delaiRenvoi == null
        || delaiRenvoi.isNegative()
        || delaiRenvoi.isZero()
        || delaiRenvoi.compareTo(dureeCode) > 0
        || maxTentatives < 1
        || maxTentatives > 20) {
      throw new IllegalArgumentException("Configuration des codes SMS invalide");
    }
  }
}
