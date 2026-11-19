package com.fitombo.app.repository.reproduction;

import com.fitombo.app.model.evenement.Saillie;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

public interface ReproductionRepository extends Repository<Saillie, UUID> {
  Optional<Saillie> findByIdAndStatutAndDeletedAtIsNull(UUID id, String statut);

  @Query(
      """
      select count(m) > 0 from MiseBas m
      where m.saillie.id = :saillieId and m.statut = 'VALIDE' and m.deletedAt is null
      """)
  boolean miseBasExistante(UUID saillieId);
}
