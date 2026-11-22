package com.fitombo.app.repository.objectif;

import com.fitombo.app.model.objectif.Objectif;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ObjectifRepository extends JpaRepository<Objectif, UUID> {
  Page<Objectif> findByExploitationIdAndDeletedAtIsNull(UUID exploitationId, Pageable page);

  Optional<Objectif> findByIdAndDeletedAtIsNull(UUID id);

  @Query(
      """
      select o from Objectif o join o.exploitation e join e.eleveur p join p.utilisateur u
      where o.deletedAt is null and o.statut = 'EN_COURS' and o.dateCible < :jour
        and e.deletedAt is null and p.deletedAt is null and u.actif and u.deletedAt is null
      order by o.id
      """)
  Slice<Objectif> objectifsEchus(LocalDate jour, Pageable page);
}
