package com.fitombo.app.repository;

import com.fitombo.app.dto.AnimalDto;
import com.fitombo.app.model.cheptel.Animal;
import com.fitombo.app.model.cheptel.StatutAnimal;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AnimalRepository extends JpaRepository<Animal, UUID> {

  @Query(
      """
      select new com.fitombo.app.dto.AnimalDto(a.id, a.identifiant, a.nom, a.espece.code,
                                             r.nom, a.sexe, a.dateNaissance, a.statut)
      from Animal a left join a.race r
      where a.exploitation.id = :exploitationId and a.deletedAt is null
        and (:statut is null or a.statut = :statut)
      """)
  Page<AnimalDto> findParExploitation(
      @Param("exploitationId") UUID exploitationId,
      @Param("statut") StatutAnimal statut,
      Pageable pageable);

  @Query(
      """
      select new com.fitombo.app.dto.AnimalDto(a.id, a.identifiant, a.nom, a.espece.code,
                                             r.nom, a.sexe, a.dateNaissance, a.statut)
      from Animal a join a.exploitation e left join a.race r
      where a.id = :id and a.deletedAt is null and
      """
          + ExploitationRepository.PERIMETRE)
  Optional<AnimalDto> findVisibleParId(
      @Param("id") UUID id, @Param("utilisateurId") UUID utilisateurId);
}
