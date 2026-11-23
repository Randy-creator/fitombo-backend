package com.fitombo.app.repository.finance;

import com.fitombo.app.dto.finance.CreerTransactionRequest;
import com.fitombo.app.dto.finance.FiltreMarge;
import com.fitombo.app.dto.finance.MargeDto;
import com.fitombo.app.dto.finance.TransactionDto;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class FinanceRepository {
  private static final String COLONNES =
      """
      id, type, categorie, montant_ariary, date_transaction, animal_id, lot_id,
      libelle, mode_paiement, notes
      """;
  private static final String PERIMETRE =
      """
      from transaction_financiere where exploitation_id = :exploitation and deleted_at is null
      """;
  private final JdbcClient jdbc;

  public TransactionDto enregistrer(
      UUID exploitation, UUID auteur, CreerTransactionRequest demande) {
    return jdbc.sql(
            """
            insert into transaction_financiere(exploitation_id, saisi_par, type, categorie, montant_ariary,
              date_transaction, animal_id, lot_id, libelle, mode_paiement, notes)
            values (:exploitation, :auteur, :type, :categorie, :montant, :date, :animal, :lot,
              :libelle, :mode, :notes)
            returning
            """
                + COLONNES)
        .param("exploitation", exploitation)
        .param("auteur", auteur)
        .param("type", demande.categorie().sens())
        .param("categorie", demande.categorie().name())
        .param("montant", demande.montantAriary())
        .param("date", demande.dateTransaction())
        .param("animal", demande.animalId())
        .param("lot", demande.lotId())
        .param("libelle", demande.libelle())
        .param("mode", demande.modePaiement())
        .param("notes", demande.notes())
        .query(TransactionDto.class)
        .single();
  }

  public Page<TransactionDto> lister(UUID exploitation, Pageable page) {
    var lignes =
        jdbc.sql(
                "select "
                    + COLONNES
                    + PERIMETRE
                    + "order by date_transaction desc, id desc limit :limite offset :decalage")
            .param("exploitation", exploitation)
            .param("limite", page.getPageSize())
            .param("decalage", page.getOffset())
            .query(TransactionDto.class)
            .list();
    long total =
        jdbc.sql("select count(*) " + PERIMETRE)
            .param("exploitation", exploitation)
            .query(Long.class)
            .single();
    return new PageImpl<>(lignes, page, total);
  }

  public MargeDto marge(UUID exploitation, FiltreMarge filtre) {
    return jdbc.sql(
            """
            with finances as (
              select coalesce(sum(montant_ariary) filter (where type = 'RECETTE'), 0) recettes,
                     coalesce(sum(montant_ariary) filter (where type = 'DEPENSE'), 0) depenses
            """
                + PERIMETRE
                + """
                    and date_transaction between :du and :au
                    and (cast(:animal as uuid) is null or animal_id = :animal)
                    and (cast(:lot as uuid) is null or lot_id = :lot)
                ), production_periode as (
                  select sum(p.quantite) poids from production p join evenement e on e.id = p.evenement_id
                  where e.exploitation_id = :exploitation and e.statut = 'VALIDE' and e.deleted_at is null
                    and e.date_evenement between :du and :au and p.unite = 'KG'
                    and (cast(:animal as uuid) is null or e.animal_id = :animal)
                    and (cast(:lot as uuid) is null or e.lot_id = :lot)
                )
                select recettes as recettes_ariary, depenses as depenses_ariary,
                  recettes - depenses as marge_ariary, poids as poids_produit_kg,
                  round(depenses / nullif(poids, 0), 2) as cout_revient_ariary_par_kg
                from finances cross join production_periode
                """)
        .param("exploitation", exploitation)
        .param("du", filtre.du())
        .param("au", filtre.au())
        .param("animal", filtre.animalId(), java.sql.Types.OTHER)
        .param("lot", filtre.lotId(), java.sql.Types.OTHER)
        .query(MargeDto.class)
        .single();
  }
}
