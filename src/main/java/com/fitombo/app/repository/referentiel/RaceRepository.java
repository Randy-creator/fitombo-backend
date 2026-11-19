package com.fitombo.app.repository.referentiel;

import com.fitombo.app.dto.referentiel.RaceDto;
import com.fitombo.app.model.referentiel.Race;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RaceRepository extends JpaRepository<Race, UUID> {

  @Query(
      """
      select new com.fitombo.app.dto.referentiel.RaceDto(r.id, r.nom, r.origine)
      from Race r
      join r.espece e
      where r.actif = true and e.actif = true and e.code = :code
      """)
  Page<RaceDto> findActivesParEspece(@Param("code") String code, Pageable pageable);

  Optional<Race> findByIdAndEspeceCodeAndActifTrue(UUID id, String especeCode);
}
