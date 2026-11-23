package com.fitombo.app.repository.emplacement;

import com.fitombo.app.dto.emplacement.EmplacementDto;
import com.fitombo.app.model.exploitation.Emplacement;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmplacementRepository extends JpaRepository<Emplacement, UUID> {
  @Query(
      """
      select new com.fitombo.app.dto.emplacement.EmplacementDto(e.id, e.nom, e.type, e.capacite)
      from Emplacement e
      where e.exploitation.id = :exploitationId and e.deletedAt is null
      """)
  Page<EmplacementDto> findVisibles(
      @Param("exploitationId") UUID exploitationId, Pageable pageable);

  Optional<Emplacement> findByIdAndDeletedAtIsNull(UUID id);

  Optional<Emplacement> findByIdAndExploitationIdAndDeletedAtIsNull(UUID id, UUID exploitationId);
}
