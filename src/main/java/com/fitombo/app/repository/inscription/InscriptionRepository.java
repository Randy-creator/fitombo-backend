package com.fitombo.app.repository.inscription;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class InscriptionRepository {
  private final JdbcTemplate jdbc;

  public void verrouiller(String telephone) {
    jdbc.query(
        "select pg_advisory_xact_lock(hashtextextended(?, 90210))", resultat -> {}, telephone);
  }

  public boolean compteExiste(String telephone) {
    return Boolean.TRUE.equals(
        jdbc.queryForObject(
            """
            select exists (select 1 from utilisateur where telephone = ? and deleted_at is null)
            """,
            Boolean.class,
            telephone));
  }

  public Optional<Code> dernier(String telephone) {
    return jdbc
        .query(
            """
            select id, code_hash, created_at, expire_at, utilise_at, tentatives from code_verification
            where telephone = ? and objectif = 'INSCRIPTION' order by created_at desc, id desc limit 1
            """,
            (ligne, numero) ->
                new Code(
                    ligne.getObject("id", UUID.class),
                    ligne.getString("code_hash"),
                    ligne.getTimestamp("created_at").toInstant(),
                    ligne.getTimestamp("expire_at").toInstant(),
                    ligne.getTimestamp("utilise_at") != null,
                    ligne.getInt("tentatives")),
            telephone)
        .stream()
        .findFirst();
  }

  public UUID enregistrer(String telephone, String hash, Instant maintenant, Instant expiration) {
    return jdbc.queryForObject(
        """
        insert into code_verification (telephone, code_hash, objectif, created_at, expire_at)
        values (?, ?, 'INSCRIPTION', ?, ?) returning id
        """,
        UUID.class,
        telephone,
        hash,
        Timestamp.from(maintenant),
        Timestamp.from(expiration));
  }

  public void noterEchec(UUID id) {
    jdbc.update("update code_verification set tentatives = tentatives + 1 where id = ?", id);
  }

  public void consommer(UUID id, Instant maintenant) {
    jdbc.update(
        "update code_verification set utilise_at = ? where id = ?", Timestamp.from(maintenant), id);
  }

  public boolean creerCompte(
      String nom, String telephone, String email, String langue, String hash) {
    return Boolean.TRUE.equals(
        jdbc.queryForObject(
            """
            with compte as (
              insert into utilisateur (nom, telephone, email, langue, mot_de_passe_hash, telephone_verifie)
              values (?, ?, ?, ?, ?, true) on conflict do nothing returning id
            ), profil as (
              insert into eleveur (utilisateur_id) select id from compte returning id
            )
            select exists (select 1 from profil)
            """,
            Boolean.class,
            nom,
            telephone,
            email,
            langue,
            hash));
  }

  public record Code(
      UUID id, String hash, Instant cree, Instant expire, boolean utilise, int tentatives) {}
}
