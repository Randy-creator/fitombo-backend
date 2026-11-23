package com.fitombo.app.service.session;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

final class JetonRafraichissement {
  private static final SecureRandom ALEATOIRE = new SecureRandom();

  private JetonRafraichissement() {}

  static String generer() {
    byte[] octets = new byte[32];
    ALEATOIRE.nextBytes(octets);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(octets);
  }

  static String hacher(String jeton) {
    try {
      return HexFormat.of()
          .formatHex(
              MessageDigest.getInstance("SHA-256")
                  .digest(jeton.getBytes(StandardCharsets.US_ASCII)));
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException("SHA-256 indisponible", exception);
    }
  }
}
