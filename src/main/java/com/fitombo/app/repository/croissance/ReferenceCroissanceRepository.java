package com.fitombo.app.repository.croissance;

import com.fitombo.app.dto.croissance.CourbeCroissanceDto.PointReference;
import com.fitombo.app.dto.croissance.CourbeCroissanceDto.Reference;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ReferenceCroissanceRepository {
  private final JdbcClient jdbc;

  public Optional<Cible> trouverCible(UUID id, boolean estAnimal) {
    String sql =
        estAnimal
            ? "select date_naissance as origine, race_id, sexe, 1 as effectif from animal where id = :id and deleted_at is null"
            : "select date_creation as origine, race_id, 'MIXTE' as sexe, effectif_actuel as effectif from lot where id = :id and deleted_at is null";
    return jdbc.sql(sql).param("id", id).query(Cible.class).optional();
  }

  public Optional<Reference> trouverReference(UUID race, String sexe) {
    return jdbc.sql(
            """
            select id, libelle, source, valide from courbe_reference where race_id = :race and actif
              and (sexe = :sexe or sexe = 'MIXTE')
            order by valide desc, (sexe = :sexe) desc, version desc, id
            limit 1
            """)
        .param("race", race)
        .param("sexe", sexe)
        .query(Profil.class)
        .optional()
        .map(profil ->
            new Reference(
                profil.id(),
                profil.libelle(),
                profil.source(),
                profil.valide(),
                points(profil.id())));
  }

  private java.util.List<PointReference> points(UUID id) {
    return jdbc.sql(
            """
            select age_jours, poids_attendu_kg, poids_min_kg, poids_max_kg
            from point_courbe where courbe_id = :id order by age_jours
            """)
        .param("id", id)
        .query(PointReference.class)
        .list();
  }

  public record Cible(java.time.LocalDate origine, UUID raceId, String sexe, int effectif) {}

  private record Profil(UUID id, String libelle, String source, boolean valide) {}
}
