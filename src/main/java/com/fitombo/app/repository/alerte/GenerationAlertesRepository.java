package com.fitombo.app.repository.alerte;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class GenerationAlertesRepository {
  private final JdbcClient jdbc;

  public int generer(LocalDate jour) {
    return rappelsEvenements(jour) + rappelsProtocoles(jour) + peseesEnRetard(jour);
  }

  private int rappelsEvenements(LocalDate jour) {
    return jdbc.sql(
            """
            with echeances as (
              select e.id, e.exploitation_id, e.animal_id, e.lot_id, 'RAPPEL_VACCIN' as type,
                v.rappel_prevu as date, 'Rappel de vaccination enregistré' as message
              from evenement e join vaccination v on v.evenement_id = e.id
              union all
              select e.id, e.exploitation_id, e.animal_id, e.lot_id, 'MISE_BAS_PREVUE',
                s.mise_bas_prevue, 'Mise bas prévue'
              from evenement e join saillie s on s.evenement_id = e.id
              where not exists (
                select 1 from mise_bas m join evenement b on b.id = m.evenement_id
                where m.saillie_id = s.evenement_id and b.statut = 'VALIDE' and b.deleted_at is null
              )
              and not exists (
                select 1 from diagnostic_gestation d join evenement g on g.id = d.evenement_id
                where d.saillie_id = s.evenement_id and d.positif = false
                  and g.statut = 'VALIDE' and g.deleted_at is null
                  and not exists (
                    select 1 from diagnostic_gestation p join evenement h on h.id = p.evenement_id
                    where p.saillie_id = s.evenement_id and p.positif and h.statut = 'VALIDE'
                      and h.deleted_at is null and h.date_evenement >= g.date_evenement
                  )
              )
              union all
              select e.id, e.exploitation_id, e.animal_id, e.lot_id, 'DELAI_ATTENTE',
                t.fin_delai_attente, 'Fin du délai d’attente enregistré'
              from evenement e join traitement t on t.evenement_id = e.id
              where t.delai_attente_jours > 0
            )
            insert into alerte (utilisateur_id, exploitation_id, type, date_echeance, message,
              cle_deduplication, animal_id, lot_id, evenement_id)
            select u.id, x.id, d.type, d.date, d.message,
              d.type || ':' || d.id || ':' || d.date, d.animal_id, d.lot_id, d.id
            from echeances d
            join evenement e on e.id = d.id
            join exploitation x on x.id = d.exploitation_id
            join eleveur p on p.id = x.eleveur_id
            join utilisateur u on u.id = p.utilisateur_id
            left join animal a on a.id = d.animal_id
            left join lot l on l.id = d.lot_id
            where d.date between :jour and cast(:jour as date) + 7
              and e.statut = 'VALIDE' and e.deleted_at is null
              and x.deleted_at is null and p.deleted_at is null and u.deleted_at is null and u.actif
              and ((a.statut = 'ACTIF' and a.deleted_at is null) or (l.statut = 'ACTIF' and l.deleted_at is null))
            on conflict on constraint uq_alerte_deduplication do nothing
            """)
        .param("jour", jour)
        .update();
  }

  private int rappelsProtocoles(LocalDate jour) {
    return jdbc.sql(
            """
            with cibles as (
              select id as animal_id, null::uuid as lot_id, date_naissance as origine,
                espece_code, exploitation_id from animal where statut = 'ACTIF' and deleted_at is null
              union all
              select null::uuid, id, date_creation, espece_code, exploitation_id
              from lot where statut = 'ACTIF' and deleted_at is null
            )
            insert into alerte (utilisateur_id, exploitation_id, type, date_echeance, message,
              cle_deduplication, animal_id, lot_id)
            select u.id, x.id,
              case when t.type_acte = 'VACCINATION' then 'VACCIN_A_FAIRE' else 'AUTRE' end,
              c.origine + t.age_jours, 'Étape du protocole : ' || t.produit,
              'PROTOCOLE:' || t.id || ':' || coalesce(c.animal_id, c.lot_id)
                || ':' || (c.origine + t.age_jours), c.animal_id, c.lot_id
            from cibles c
            join protocole_sanitaire p on p.espece_code = c.espece_code and p.actif and p.valide
            join etape_protocole t on t.protocole_id = p.id
            join exploitation x on x.id = c.exploitation_id
            join eleveur el on el.id = x.eleveur_id
            join utilisateur u on u.id = el.utilisateur_id
            where c.origine + t.age_jours between :jour and cast(:jour as date) + 7
              and x.deleted_at is null and el.deleted_at is null and u.deleted_at is null and u.actif
            on conflict on constraint uq_alerte_deduplication do nothing
            """)
        .param("jour", jour)
        .update();
  }

  private int peseesEnRetard(LocalDate jour) {
    return jdbc.sql(
            """
            with cibles as (
              select id as animal_id, null::uuid as lot_id, coalesce(date_naissance, cast(created_at as date)) origine,
                espece_code, exploitation_id from animal where statut = 'ACTIF' and deleted_at is null
              union all
              select null::uuid, id, date_creation, espece_code, exploitation_id
              from lot where statut = 'ACTIF' and deleted_at is null
            ), echeances as (
              select c.*, greatest(c.origine, (
                select max(e.date_evenement) from evenement e
                where e.statut = 'VALIDE' and e.deleted_at is null and e.type = 'PESEE'
                  and e.date_evenement <= :jour
                  and (e.animal_id = c.animal_id or e.lot_id = c.lot_id)
              )) + es.frequence_pesee_jours as echeance
              from cibles c join espece es on es.code = c.espece_code and es.actif
              where es.frequence_pesee_jours is not null
            )
            insert into alerte (utilisateur_id, exploitation_id, type, date_echeance, message,
              cle_deduplication, animal_id, lot_id)
            select u.id, x.id, 'PESEE_EN_RETARD', c.echeance, 'Pesée à renouveler',
              'PESEE:' || coalesce(c.animal_id, c.lot_id) || ':' || c.echeance, c.animal_id, c.lot_id
            from echeances c join exploitation x on x.id = c.exploitation_id
            join eleveur p on p.id = x.eleveur_id join utilisateur u on u.id = p.utilisateur_id
            where c.echeance < :jour and x.deleted_at is null and p.deleted_at is null
              and u.deleted_at is null and u.actif
            on conflict on constraint uq_alerte_deduplication do nothing
            """)
        .param("jour", jour)
        .update();
  }
}
