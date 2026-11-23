package com.fitombo.app.repository.organisation;

import com.fitombo.app.dto.organisation.PartageDto;
import com.fitombo.app.dto.organisation.StatistiquesOrganisationDto;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PartageRepository {
  private final JdbcClient jdbc;

  public Optional<PartageDto> consulter(UUID utilisateur) {
    return jdbc.sql(
            """
            select o.id as organisation_id, o.nom as organisation,
              coalesce(e.partage_donnees_organisation and o.id = e.organisation_consentie_id, false) as partage
            from eleveur e join utilisateur u on u.id = e.utilisateur_id
            left join organisation o on o.id = e.organisation_id and o.deleted_at is null
            where u.id = :utilisateur and u.actif and u.deleted_at is null and e.deleted_at is null
            """)
        .param("utilisateur", utilisateur)
        .query(PartageDto.class)
        .optional();
  }

  public int modifier(UUID utilisateur, boolean partage, UUID organisation) {
    return jdbc.sql(
            """
            update eleveur e set partage_donnees_organisation = :partage,
              organisation_consentie_id = case when :partage then e.organisation_id else null end,
              version = e.version + 1
            from utilisateur u
            where u.id = :utilisateur and e.utilisateur_id = u.id
              and u.actif and u.deleted_at is null and e.deleted_at is null
              and (not :partage or exists (
                select 1 from organisation o where o.id = e.organisation_id and o.id = :organisation and o.deleted_at is null))
            """)
        .param("utilisateur", utilisateur)
        .param("partage", partage)
        .param("organisation", organisation)
        .update();
  }

  public Optional<StatistiquesOrganisationDto> statistiques(UUID utilisateur, int minimum) {
    return jdbc.sql(
            """
            with organisation_autorisee as (
              select o.id from utilisateur u join organisation o on o.id = u.organisation_id
              where u.id = :utilisateur and u.role = 'ORGANISATION' and u.actif
                and u.deleted_at is null and o.deleted_at is null
            ), participants as (
              select e.id from eleveur e join organisation_autorisee o on o.id = e.organisation_id
              join utilisateur u on u.id = e.utilisateur_id
              where e.organisation_consentie_id = o.id and e.partage_donnees_organisation
                and e.deleted_at is null and u.deleted_at is null and u.actif
            ), fermes as (
              select f.id from exploitation f join participants p on p.id = f.eleveur_id
              where f.deleted_at is null
            ), effectifs as (
              select a.exploitation_id, 1 as effectif from animal a
              where a.statut = 'ACTIF' and a.deleted_at is null and a.lot_id is null
              union all
              select l.exploitation_id, l.effectif_actuel from lot l
              where l.statut = 'ACTIF' and l.deleted_at is null
            )
            select (select count(*) from participants) as eleveurs,
              (select count(*) from fermes) as exploitations,
              (select coalesce(sum(t.effectif), 0) from effectifs t join fermes f on f.id = t.exploitation_id)
                as effectif_actif
            from organisation_autorisee
            where (select count(*) from participants) >= :minimum
            """)
        .param("utilisateur", utilisateur)
        .param("minimum", minimum)
        .query(StatistiquesOrganisationDto.class)
        .optional();
  }
}
