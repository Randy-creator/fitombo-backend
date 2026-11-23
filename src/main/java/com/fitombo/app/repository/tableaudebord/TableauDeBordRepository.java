package com.fitombo.app.repository.tableaudebord;

import com.fitombo.app.dto.tableaudebord.TableauDeBordDto.EffectifEspece;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TableauDeBordRepository {
  private final JdbcClient jdbc;

  public List<EffectifEspece> effectifs(UUID exploitation) {
    return jdbc.sql(
            """
            select espece_code, sum(effectif) effectif from (
              select espece_code, effectif_actuel effectif from lot
              where exploitation_id = :id and statut = 'ACTIF' and deleted_at is null
              union all
              select espece_code, 1 from animal
              where exploitation_id = :id and statut = 'ACTIF' and deleted_at is null and lot_id is null
            ) cheptel group by espece_code order by espece_code
            """)
        .param("id", exploitation)
        .query(EffectifEspece.class)
        .list();
  }

  public long alertesDuJour(UUID exploitation, UUID auteur, LocalDate jour) {
    return jdbc.sql(
            """
            select count(*) from alerte where exploitation_id = :id and utilisateur_id = :auteur
              and date_echeance = :jour and statut in ('EN_ATTENTE', 'ENVOYEE', 'VUE')
            """)
        .param("id", exploitation)
        .param("auteur", auteur)
        .param("jour", jour)
        .query(Long.class)
        .single();
  }

  public long deces(UUID exploitation, LocalDate du, LocalDate au) {
    return jdbc.sql(
            """
            select coalesce(sum(d.nombre), 0) from deces d join evenement e on e.id = d.evenement_id
            where e.exploitation_id = :id and e.statut = 'VALIDE' and e.deleted_at is null
              and e.date_evenement between :du and :au and d.cause not in ('PERTE', 'VOL')
            """)
        .param("id", exploitation)
        .param("du", du)
        .param("au", au)
        .query(Long.class)
        .single();
  }
}
