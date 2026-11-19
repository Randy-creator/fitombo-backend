package com.fitombo.app.repository.mouvement;

import com.fitombo.app.model.evenement.Traitement;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

public interface DelaiAttenteRepository extends Repository<Traitement, UUID> {
  @Query(
      value =
          """
          select max(t.fin_delai_attente) from traitement t join evenement e on e.id = t.evenement_id
          where e.statut = 'VALIDE' and e.deleted_at is null and e.exploitation_id = :exploitationId
            and e.date_evenement <= :date and t.fin_delai_attente > :date
            and (e.animal_id = :animalId or e.lot_id = :lotId
                 or exists (select 1 from animal a where a.id = e.animal_id
                              and a.lot_id = :lotId and a.deleted_at is null))
          """,
      nativeQuery = true)
  LocalDate finDelai(UUID exploitationId, UUID animalId, UUID lotId, LocalDate date);
}
