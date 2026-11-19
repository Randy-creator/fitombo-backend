package com.fitombo.app.service.inscription;

import com.fitombo.app.config.security.JwtProperties;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.Locale;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class CodeSms {
  private final SecureRandom aleatoire = new SecureRandom();
  private final JwtProperties configuration;

  String generer() {
    return String.format(Locale.ROOT, "%06d", aleatoire.nextInt(1_000_000));
  }

  String hacher(String telephone, String code) {
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(
          new SecretKeySpec(configuration.secret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
      var message =
          ("fitombo:inscription:" + telephone + ":" + code).getBytes(StandardCharsets.UTF_8);
      return HexFormat.of().formatHex(mac.doFinal(message));
    } catch (GeneralSecurityException exception) {
      throw new IllegalStateException("Calcul du hash SMS impossible", exception);
    }
  }

  boolean correspond(String telephone, String code, String attendu) {
    return MessageDigest.isEqual(
        hacher(telephone, code).getBytes(StandardCharsets.US_ASCII),
        attendu.getBytes(StandardCharsets.US_ASCII));
  }
}
