package com.fitombo.app.repository.synchronisation;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ReceptionSyncRepository {
  private final JdbcClient jdbc;

  public boolean reserver(UUID utilisateur, UUID operation, String empreinte) {
    return jdbc.sql(
                """
                insert into reception_sync_evenement(utilisateur_id, operation_id, empreinte)
                values (:utilisateur, :operation, :empreinte)
                on conflict (utilisateur_id, operation_id) do nothing
                """)
            .param("utilisateur", utilisateur)
            .param("operation", operation)
            .param("empreinte", empreinte)
            .update()
        == 1;
  }

  public Reception retrouver(UUID utilisateur, UUID operation) {
    return jdbc.sql(
            """
            select empreinte, evenement_id from reception_sync_evenement
            where utilisateur_id = :utilisateur and operation_id = :operation
            """)
        .param("utilisateur", utilisateur)
        .param("operation", operation)
        .query(Reception.class)
        .single();
  }

  public void terminer(UUID utilisateur, UUID operation, UUID evenement) {
    jdbc.sql(
            """
            update reception_sync_evenement set evenement_id = :evenement
            where utilisateur_id = :utilisateur and operation_id = :operation
            """)
        .param("utilisateur", utilisateur)
        .param("operation", operation)
        .param("evenement", evenement)
        .update();
  }

  public record Reception(String empreinte, UUID evenementId) {}
}
