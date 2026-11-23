package com.fitombo.app.repository.historique;

import com.fitombo.app.dto.historique.EvenementHistoriqueDto;
import com.fitombo.app.dto.historique.FiltresEvenements;
import com.fitombo.app.model.evenement.Evenement;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface HistoriqueRepository extends Repository<Evenement, UUID> {
  @Query(
      """
      select new com.fitombo.app.dto.historique.EvenementHistoriqueDto(e.id, e.typeEvenement,
          e.dateEvenement, e.statut, e.notes, e.corrigeEvenement.id)
      from Evenement e
      where e.deletedAt is null
        and (:#{#filtres.animalId} is null or e.animal.id = :#{#filtres.animalId})
        and (:#{#filtres.lotId} is null or e.lot.id = :#{#filtres.lotId})
        and (:#{#filtres.type} is null or e.typeEvenement = :#{#filtres.type})
        and e.dateEvenement >= coalesce(:#{#filtres.du}, e.dateEvenement)
        and e.dateEvenement <= coalesce(:#{#filtres.au}, e.dateEvenement)
      """)
  Page<EvenementHistoriqueDto> findHistorique(
      @Param("filtres") FiltresEvenements filtres, Pageable pageable);
}
