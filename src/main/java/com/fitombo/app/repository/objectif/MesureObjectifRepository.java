package com.fitombo.app.repository.objectif;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MesureObjectifRepository {
  private final JdbcClient jdbc;

  public BigDecimal effectif(UUID exploitation, UUID animal, UUID lot) {
    return jdbc.sql(
            """
            select coalesce((select sum(l.effectif_actuel) from lot l where l.exploitation_id = :exploitation
                and l.deleted_at is null and l.statut = 'ACTIF' and (cast(:lot as uuid) is null or l.id = :lot)
                and cast(:animal as uuid) is null), 0)
                 + (select count(*) from animal a where a.exploitation_id = :exploitation
                     and a.deleted_at is null and a.statut = 'ACTIF' and cast(:lot as uuid) is null
                     and ((cast(:animal as uuid) is null and a.lot_id is null) or a.id = :animal))
            """)
        .param("exploitation", exploitation)
        .param("animal", animal, java.sql.Types.OTHER)
        .param("lot", lot, java.sql.Types.OTHER)
        .query(BigDecimal.class)
        .single();
  }

  public BigDecimal production(
      UUID exploitation, UUID animal, UUID lot, LocalDate debut, LocalDate fin, String type) {
    return jdbc.sql(
            """
            select sum(p.quantite) as quantite from production p join evenement e on e.id = p.evenement_id
            where e.exploitation_id = :exploitation and e.deleted_at is null and e.statut = 'VALIDE'
              and e.date_evenement between :debut and :fin and p.type_production = :type
              and (cast(:animal as uuid) is null or e.animal_id = :animal) and (cast(:lot as uuid) is null or e.lot_id = :lot)
            group by e.date_evenement order by e.date_evenement desc limit 1
            """)
        .param("exploitation", exploitation)
        .param("animal", animal, java.sql.Types.OTHER)
        .param("lot", lot, java.sql.Types.OTHER)
        .param("debut", debut)
        .param("fin", fin)
        .param("type", type)
        .query(BigDecimal.class)
        .optional()
        .orElse(null);
  }

  public BigDecimal marge(
      UUID exploitation,
      UUID animal,
      UUID lot,
      LocalDate debut,
      LocalDate fin,
      boolean enPourcentage) {
    return jdbc.sql(
            """
            select case when :pourcentage then
              100 * sum(case when type = 'RECETTE' then montant_ariary else -montant_ariary end)
                 / nullif(sum(montant_ariary) filter (where type = 'RECETTE'), 0)
            else sum(case when type = 'RECETTE' then montant_ariary else -montant_ariary end) end
            from transaction_financiere where exploitation_id = :exploitation and deleted_at is null
              and date_transaction between :debut and :fin
              and (cast(:animal as uuid) is null or animal_id = :animal) and (cast(:lot as uuid) is null or lot_id = :lot)
            """)
        .param("exploitation", exploitation)
        .param("animal", animal, java.sql.Types.OTHER)
        .param("lot", lot, java.sql.Types.OTHER)
        .param("debut", debut)
        .param("fin", fin)
        .param("pourcentage", enPourcentage)
        .query(BigDecimal.class)
        .optional()
        .orElse(null);
  }
}
