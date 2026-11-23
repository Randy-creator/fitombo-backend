package com.fitombo.app.repository.alerte;

import com.fitombo.app.dto.alerte.AlerteDto;
import com.fitombo.app.model.alerte.Alerte;
import com.fitombo.app.model.alerte.StatutAlerte;
import com.fitombo.app.repository.exploitation.ExploitationRepository;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface AlerteRepository extends Repository<Alerte, UUID> {
  String PERIMETRE =
      """
      a.utilisateur.id = :utilisateurId
        and a.utilisateur.actif = true and a.utilisateur.deletedAt is null
        and (
          a.exploitation is null or exists (
            select 1 from Exploitation e where e = a.exploitation and
      """
          + ExploitationRepository.PERIMETRE
          + "))";

  @Query(
      """
      select new com.fitombo.app.dto.alerte.AlerteDto(a.id, a.type, a.dateEcheance, a.statut, a.message)
      from Alerte a where (:statut is null or a.statut = :statut) and
      """
          + PERIMETRE)
  Page<AlerteDto> findVisibles(
      @Param("utilisateurId") UUID utilisateurId,
      @Param("statut") StatutAlerte statut,
      Pageable pageable);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select a from Alerte a where a.id = :id and " + PERIMETRE)
  Optional<Alerte> findAModifier(@Param("id") UUID id, @Param("utilisateurId") UUID utilisateurId);

  @Modifying
  @Query(
      value =
          """
          update alerte set statut = 'IGNOREE'
          where evenement_id = :evenement and statut in ('EN_ATTENTE', 'ENVOYEE', 'VUE')
          """,
      nativeQuery = true)
  void ignorerLieesAEvenement(UUID evenement);
}
