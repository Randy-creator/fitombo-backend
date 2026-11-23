package com.fitombo.app.repository.referentiel;

import com.fitombo.app.dto.referentiel.EtapeSanitaireDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ProtocoleRepository {
  private final JdbcClient jdbc;

  public List<EtapeSanitaireDto> etapes(String espece) {
    return jdbc.sql(
            """
            select p.id as protocole_id, p.nom as protocole, p.source, p.valide,
                   e.id, e.ordre, e.age_jours, e.type_acte, e.produit, e.rappel_apres_jours
            from protocole_sanitaire p join etape_protocole e on e.protocole_id = p.id
            where p.espece_code = :espece and p.actif
            order by p.nom, p.id, e.ordre
            """)
        .param("espece", espece)
        .query(EtapeSanitaireDto.class)
        .list();
  }
}
