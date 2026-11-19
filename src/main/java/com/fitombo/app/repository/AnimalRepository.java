package com.fitombo.app.repository;

import com.fitombo.app.dto.AnimalDto;
import com.fitombo.app.dto.animal.FiltresAnimaux;
import com.fitombo.app.model.cheptel.Animal;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AnimalRepository extends JpaRepository<Animal, UUID> {

  String PROJECTION =
      """
      select new com.fitombo.app.dto.AnimalDto(a.id, a.identifiant, a.nom, a.espece.code,
                                             r.nom, a.sexe, a.dateNaissance, a.statut)
      """;

  @Query(
      PROJECTION
          + """
          from Animal a left join a.race r
          where a.exploitation.id = :exploitationId and a.deletedAt is null
            and (:#{#filtres.statut} is null or a.statut = :#{#filtres.statut})
            and (:#{#filtres.especeCode} is null or a.espece.code = :#{#filtres.especeCode})
            and (:#{#filtres.emplacementId} is null or a.emplacement.id = :#{#filtres.emplacementId})
            and (:#{#filtres.sexe?.name()} is null or a.sexe = :#{#filtres.sexe?.name()})
            and (:#{#filtres.ageMinJours} is null
              or timestampdiff(day, a.dateNaissance, current_date) >= :#{#filtres.ageMinJours})
            and (:#{#filtres.ageMaxJours} is null
              or timestampdiff(day, a.dateNaissance, current_date) <= :#{#filtres.ageMaxJours})
            and (:#{#filtres.recherche} is null
              or locate(lower(:#{#filtres.recherche}), lower(a.identifiant)) > 0
              or locate(lower(:#{#filtres.recherche}), lower(a.nom)) > 0)
          """)
  Page<AnimalDto> findParExploitation(
      @Param("exploitationId") UUID exploitationId,
      @Param("filtres") FiltresAnimaux filtres,
      Pageable pageable);

  @Query(
      PROJECTION
          + """
          from Animal a join a.exploitation e left join a.race r
          where a.id = :id and a.deletedAt is null and
          """
          + ExploitationRepository.PERIMETRE)
  Optional<AnimalDto> findVisibleParId(
      @Param("id") UUID id, @Param("utilisateurId") UUID utilisateurId);

  @Query(
      """
      select a from Animal a join a.exploitation e
      where a.id = :id and a.espece.code = :especeCode and a.sexe = :sexe
        and a.deletedAt is null and
      """
          + ExploitationRepository.PERIMETRE)
  Optional<Animal> findParentVisible(
      @Param("id") UUID id,
      @Param("utilisateurId") UUID utilisateurId,
      @Param("especeCode") String especeCode,
      @Param("sexe") String sexe);
}
