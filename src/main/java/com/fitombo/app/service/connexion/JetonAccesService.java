package com.fitombo.app.service.connexion;

import com.fitombo.app.config.security.JwtProperties;
import com.fitombo.app.dto.connexion.ConnexionResponse;
import com.fitombo.app.model.utilisateur.Role;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
class JetonAccesService {

  private final JwtEncoder encodeur;
  private final JwtProperties configuration;

  ConnexionResponse emettre(UUID utilisateurId, Role role) {
    var duree = configuration.accessTokenTtl();
    Instant maintenant = Instant.now();
    var claims =
        JwtClaimsSet.builder()
            .issuer(configuration.issuer())
            .subject(utilisateurId.toString())
            .issuedAt(maintenant)
            .expiresAt(maintenant.plus(duree))
            .claim("role", role.name())
            .claim("token_use", "access")
            .build();
    var header = JwsHeader.with(MacAlgorithm.HS256).type("JWT").build();
    var jeton = encodeur.encode(JwtEncoderParameters.from(header, claims));
    return new ConnexionResponse(jeton.getTokenValue(), "Bearer", duree.toSeconds());
  }
}
