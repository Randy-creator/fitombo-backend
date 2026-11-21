package com.fitombo.app.repository.lot;

import com.fitombo.app.dto.lot.FicheLotDto;
import com.fitombo.app.dto.lot.LotDto;
import com.fitombo.app.model.cheptel.Lot;
import com.fitombo.app.model.cheptel.StatutLot;
import com.fitombo.app.repository.exploitation.ExploitationRepository;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
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

  @Query(
      """
      select new com.fitombo.app.dto.lot.FicheLotDto(l.id, e.id, l.code, l.espece.code,
        l.race.id, l.emplacement.id, l.dateCreation, l.dateCloture, l.effectifInitial,
        l.effectifActuel, l.statut)
      from Lot l join l.exploitation e where l.id = :id and l.deletedAt is null and
      """
          + ExploitationRepository.PERIMETRE)
  Optional<FicheLotDto> trouverFiche(UUID id, UUID utilisateurId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query(
      """
      select l from Lot l join l.exploitation e
      where l.id = :id and l.deletedAt is null
        and e.eleveur.utilisateur.id = :utilisateurId and e.eleveur.deletedAt is null and
      """
          + ExploitationRepository.PERIMETRE)
  Optional<Lot> findPourGestion(@Param("id") UUID id, @Param("utilisateurId") UUID utilisateurId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  Optional<Lot> findByIdAndExploitationIdAndDeletedAtIsNull(UUID id, UUID exploitationId);

  @Query(
      """
      select count(l) > 0 from Lot l join l.exploitation e
      where l.id = :id and l.deletedAt is null and
      """
          + ExploitationRepository.PERIMETRE)
  boolean existeVisible(@Param("id") UUID id, @Param("utilisateurId") UUID utilisateurId);

  boolean existsByIdAndExploitationIdAndDeletedAtIsNull(UUID id, UUID exploitationId);
}
