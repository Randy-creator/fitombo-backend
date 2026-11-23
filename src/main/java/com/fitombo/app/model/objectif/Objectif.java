package com.fitombo.app.model.objectif;

import com.fitombo.app.model.cheptel.Animal;
import com.fitombo.app.model.cheptel.Lot;
import com.fitombo.app.model.commun.EntiteUuid;
import com.fitombo.app.model.exploitation.Exploitation;
import com.fitombo.app.model.referentiel.CourbeReference;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "objectif")
public class Objectif extends EntiteUuid {

  @Enumerated(EnumType.STRING)
  private TypeObjectif type;

  @Column(name = "valeur_cible")
  private BigDecimal valeurCible;

  @Column(name = "date_cible")
  private LocalDate dateCible;

  private String statut;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "exploitation_id", nullable = false)
  private Exploitation exploitation;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "animal_id")
  private Animal animal;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "lot_id")
  private Lot lot;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "courbe_reference_id")
  private CourbeReference courbeReference;

  @Column(name = "valeur_initiale")
  private BigDecimal valeurInitiale;

  @Column(name = "date_debut")
  private LocalDate dateDebut;

  @Enumerated(EnumType.STRING)
  private UniteObjectif unite;

  private String notes;

  @Column(name = "deleted_at")
  private java.time.Instant deletedAt;

  @Version private Long version;

  public void modifier(BigDecimal valeur, LocalDate date, String notes) {
    valeurCible = valeur;
    dateCible = date;
    this.notes = notes;
  }

  public void abandonner() {
    statut = "ABANDONNE";
    deletedAt = java.time.Instant.now();
  }

  public Objectif(
      Exploitation exploitation,
      Animal animal,
      Lot lot,
      TypeObjectif type,
      BigDecimal valeurInitiale,
      BigDecimal valeurCible,
      UniteObjectif unite,
      LocalDate dateDebut,
      LocalDate dateCible,
      String notes) {
    this.exploitation = exploitation;
    this.animal = animal;
    this.lot = lot;
    this.type = type;
    this.valeurInitiale = valeurInitiale;
    this.valeurCible = valeurCible;
    this.unite = unite;
    this.dateDebut = dateDebut;
    this.dateCible = dateCible;
    this.notes = notes;
    this.statut = "EN_COURS";
  }
}
