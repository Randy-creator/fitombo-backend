package com.fitombo.app.repository.evenement;

import com.fitombo.app.model.evenement.Evenement;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface EvenementRepository extends JpaRepository<Evenement, UUID> {
  @Query("select e.exploitation.id from Evenement e where e.id = :id and e.deletedAt is null")
  Optional<UUID> trouverExploitation(UUID id);

  @Query(
      value =
          """
          select id from evenement where id = :id and deleted_at is null for update
          """,
      nativeQuery = true)
  Optional<UUID> verrouiller(UUID id);
}
