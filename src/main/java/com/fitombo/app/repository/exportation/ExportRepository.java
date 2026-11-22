package com.fitombo.app.repository.exportation;

import com.fitombo.app.dto.exportation.PeriodeExport;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ExportRepository {
  private final JdbcClient jdbc;

  public List<EvenementExport> evenements(
      UUID exploitation, UUID animal, UUID lot, PeriodeExport periode, int limite) {
    return jdbc.sql(
            """
            select e.date_evenement as date, e.type, e.statut, coalesce(a.identifiant, l.code) cible,
              case e.type
                when 'PESEE' then concat(p.poids_kg, ' kg (', p.methode, ')')
                when 'VACCINATION' then v.vaccin
                when 'TRAITEMENT' then t.produit
                when 'ALIMENTATION' then concat(al.aliment, ' : ', al.quantite_kg, ' kg')
                when 'PRODUCTION' then concat(pr.type_production, ' : ', pr.quantite, ' ', pr.unite)
                when 'MOUVEMENT' then concat(m.type_mouvement, ' (', m.quantite, ')')
                when 'DECES' then concat(d.cause, ' (', d.nombre, ')')
                when 'SAILLIE' then s.methode
                when 'DIAGNOSTIC_GESTATION' then case when g.positif then 'Positif' else 'Négatif' end
                when 'MISE_BAS' then concat(b.nes_vivants, ' nés vivants, ', b.morts_nes, ' mort-nés')
              end details, e.notes
            from evenement e
            left join animal a on a.id = e.animal_id
            left join lot l on l.id = e.lot_id
            left join pesee p on p.evenement_id = e.id
            left join vaccination v on v.evenement_id = e.id
            left join traitement t on t.evenement_id = e.id
            left join alimentation al on al.evenement_id = e.id
            left join production pr on pr.evenement_id = e.id
            left join mouvement m on m.evenement_id = e.id
            left join deces d on d.evenement_id = e.id
            left join saillie s on s.evenement_id = e.id
            left join diagnostic_gestation g on g.evenement_id = e.id
            left join mise_bas b on b.evenement_id = e.id
            where e.deleted_at is null
              and (cast(:exploitation as uuid) is null or e.exploitation_id = :exploitation)
              and (cast(:animal as uuid) is null or e.animal_id = :animal)
              and (cast(:lot as uuid) is null or e.lot_id = :lot)
              and e.date_evenement >= coalesce(cast(:du as date), e.date_evenement)
              and e.date_evenement <= coalesce(cast(:au as date), e.date_evenement)
            order by e.date_evenement, e.id limit :limite
            """)
        .param("exploitation", exploitation)
        .param("animal", animal)
        .param("lot", lot)
        .param("du", periode.du())
        .param("au", periode.au())
        .param("limite", limite)
        .query(EvenementExport.class)
        .list();
  }

  public List<TransactionExport> transactions(
      UUID exploitation, PeriodeExport periode, int limite) {
    return jdbc.sql(
            """
            select t.date_transaction as date, t.type, t.categorie, t.montant_ariary, coalesce(a.identifiant, l.code) cible,
              t.libelle, t.mode_paiement, t.notes
            from transaction_financiere t
            left join animal a on a.id = t.animal_id
            left join lot l on l.id = t.lot_id
            where t.exploitation_id = :exploitation and t.deleted_at is null
              and t.date_transaction >= coalesce(cast(:du as date), t.date_transaction)
              and t.date_transaction <= coalesce(cast(:au as date), t.date_transaction)
            order by t.date_transaction, t.id limit :limite
            """)
        .param("exploitation", exploitation)
        .param("du", periode.du())
        .param("au", periode.au())
        .param("limite", limite)
        .query(TransactionExport.class)
        .list();
  }

  public record EvenementExport(
      LocalDate date, String type, String statut, String cible, String details, String notes) {}

  public record TransactionExport(
      LocalDate date,
      String type,
      String categorie,
      BigDecimal montantAriary,
      String cible,
      String libelle,
      String modePaiement,
      String notes) {}
}
