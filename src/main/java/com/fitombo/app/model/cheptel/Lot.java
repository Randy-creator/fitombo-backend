package com.fitombo.app.model.cheptel;

import com.fitombo.app.model.commun.EntiteUuid;
import com.fitombo.app.model.exploitation.Emplacement;
import com.fitombo.app.model.exploitation.Exploitation;
import com.fitombo.app.model.referentiel.Espece;
import com.fitombo.app.model.referentiel.Race;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "lot")
public class Lot extends EntiteUuid {

  private String code;

  @Column(name = "date_creation")
  private LocalDate dateCreation;

  @Column(name = "effectif_initial")
  private Integer effectifInitial;

  @Column(name = "effectif_actuel")
  private Integer effectifActuel;

  @Enumerated(EnumType.STRING)
  private StatutLot statut;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "exploitation_id", nullable = false)
  private Exploitation exploitation;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "espece_code", nullable = false)
  private Espece espece;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "race_id")
  private Race race;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "emplacement_id")
  private Emplacement emplacement;

  @Column(name = "deleted_at")
  private Instant deletedAt;

  @Version private Long version;

  public Lot(
      Exploitation exploitation,
      Espece espece,
      Race race,
      Emplacement emplacement,
      String code,
      LocalDate dateCreation,
      int effectifInitial) {
    this.exploitation = exploitation;
    this.espece = espece;
    this.race = race;
    this.emplacement = emplacement;
    this.code = code.strip();
    this.dateCreation = dateCreation;
    this.effectifInitial = effectifInitial;
    this.effectifActuel = effectifInitial;
    this.statut = StatutLot.ACTIF;
  }
}
