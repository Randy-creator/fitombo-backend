package com.fitombo.app.repository.session;

import com.fitombo.app.model.utilisateur.Role;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SessionRepository {
  private final JdbcTemplate jdbc;

  public void verrouiller(UUID utilisateurId) {
    jdbc.query(
        "select pg_advisory_xact_lock(hashtextextended(?, 90211))",
        resultat -> {},
        utilisateurId.toString());
  }

  public Optional<Session> chercher(String hash) {
    return jdbc
        .query(
            """
            select j.id, j.utilisateur_id, j.expire_at, j.revoque_at, u.role,
                   u.actif and (u.telephone_verifie or u.email_verifie) and u.deleted_at is null as compte_valide
            from jeton_rafraichissement j join utilisateur u on u.id = j.utilisateur_id
            where j.jeton_hash = ?
            """,
            (ligne, numero) ->
                new Session(
                    ligne.getObject("id", UUID.class),
                    ligne.getObject("utilisateur_id", UUID.class),
                    ligne.getTimestamp("expire_at").toInstant(),
                    ligne.getTimestamp("revoque_at") != null,
                    Role.valueOf(ligne.getString("role")),
                    ligne.getBoolean("compte_valide")),
            hash)
        .stream()
        .findFirst();
  }

  public UUID creer(UUID utilisateurId, String hash, Instant expiration) {
    return jdbc.queryForObject(
        """
        insert into jeton_rafraichissement (utilisateur_id, jeton_hash, expire_at)
        values (?, ?, ?) returning id
        """,
        UUID.class,
        utilisateurId,
        hash,
        Timestamp.from(expiration));
  }

  public void remplacer(UUID id, UUID suivant) {
    jdbc.update(
        """
        update jeton_rafraichissement set revoque_at = now(), remplace_par_id = ? where id = ?
        """,
        suivant,
        id);
  }

  public void revoquerChaine(UUID id) {
    jdbc.update(
        """
        with recursive chaine(id, suivant) as (
          select id, remplace_par_id from jeton_rafraichissement where id = ?
          union
          select j.id, j.remplace_par_id from jeton_rafraichissement j join chaine c on j.id = c.suivant
        )
        update jeton_rafraichissement set revoque_at = now()
        where id in (select id from chaine) and revoque_at is null
        """,
        id);
  }

  public record Session(
      UUID id,
      UUID utilisateurId,
      Instant expiration,
      boolean revoquee,
      Role role,
      boolean compteValide) {}
}
