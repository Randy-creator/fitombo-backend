package com.fitombo.app.repository.synchronisation;

import com.fitombo.app.dto.synchronisation.FenetreSynchronisation;
import com.fitombo.app.dto.synchronisation.PageSynchronisation.Changement;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JournalSyncRepository {
  private final JdbcClient jdbc;

  public List<Changement> changements(
      UUID exploitation, FenetreSynchronisation curseur, int limite) {
    return jdbc.sql(
            """
            select j.transaction_id::text as transaction, j.sequence, j.evenement_id,
              e.id is null as retire, e.type, e.statut, e.animal_id, e.lot_id,
              e.date_evenement, e.notes, e.corrige_evenement_id
            from journal_sync_evenement j
            left join evenement e on e.id = j.evenement_id and e.exploitation_id = j.exploitation_id
              and e.deleted_at is null
            where j.exploitation_id = :exploitation
              and (j.transaction_id, j.sequence) > (cast(:transaction as xid8), :sequence)
              and j.transaction_id < pg_snapshot_xmin(pg_current_snapshot())
            order by j.transaction_id, j.sequence
            limit :limite
            """)
        .param("exploitation", exploitation)
        .param("transaction", curseur.transactionEffective())
        .param("sequence", curseur.sequenceEffective())
        .param("limite", limite)
        .query(Changement.class)
        .list();
  }
}
