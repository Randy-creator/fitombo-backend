package com.fitombo.app.repository;

import com.fitombo.app.dto.EspeceDto;
import com.fitombo.app.model.referentiel.Espece;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface EspeceRepository extends JpaRepository<Espece, String> {

  boolean existsByCodeAndActifTrue(String code);

  @Query(
      """
      select new com.fitombo.app.dto.EspeceDto(e.code, e.nomFr, e.nomMg, e.modeSuivi)
      from Espece e
      where e.actif = true
      """)
  Page<EspeceDto> findActives(Pageable pageable);
}
