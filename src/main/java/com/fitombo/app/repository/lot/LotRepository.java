package com.fitombo.app.repository.lot;

import com.fitombo.app.dto.lot.LotDto;
import com.fitombo.app.model.cheptel.Lot;
import com.fitombo.app.model.cheptel.StatutLot;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LotRepository extends JpaRepository<Lot, UUID> {
  @Query(
      """
      select new com.fitombo.app.dto.lot.LotDto(e.id, e.code, e.espece.code, e.dateCreation, e.effectifInitial, e.effectifActuel, e.statut)
      from Lot e
      where e.exploitation.id = :exploitationId and e.deletedAt is null and (:statut is null or e.statut = :statut)
      """)
  Page<LotDto> findVisibles(
      @Param("exploitationId") UUID exploitationId,
      @Param("statut") StatutLot statut,
      Pageable pageable);
}
