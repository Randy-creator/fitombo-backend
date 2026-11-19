package com.fitombo.app.repository.croissance;

import com.fitombo.app.dto.croissance.PointPesee;
import com.fitombo.app.model.evenement.Pesee;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

public interface CroissanceRepository extends Repository<Pesee, UUID> {
  @Query(
      """
      select new com.fitombo.app.dto.croissance.PointPesee(p.dateEvenement, p.poidsKg, p.methode)
      from Pesee p where p.statut = 'VALIDE' and p.deletedAt is null
        and (p.animal.id = :animalId or p.lot.id = :lotId) and p.dateEvenement <= :date
      order by p.dateEvenement asc, p.createdAt asc, p.id asc
      """)
  List<PointPesee> trouverPesees(UUID animalId, UUID lotId, LocalDate date);

  @Query(
      """
      select sum(a.quantiteKg) from Alimentation a
      where a.lot.id = :lotId and a.statut = 'VALIDE' and a.deletedAt is null
        and a.dateEvenement > :debut and a.dateEvenement <= :fin
      """)
  BigDecimal alimentConsomme(UUID lotId, LocalDate debut, LocalDate fin);

  @Query(
      """
      select count(e) = 0 from Evenement e
      where e.lot.id = :lotId and e.statut = 'VALIDE' and e.deletedAt is null
        and e.dateEvenement >= :debut
        and (type(e) = Deces or type(e) = Mouvement)
      """)
  boolean effectifStable(UUID lotId, LocalDate debut);
}
