package com.fitombo.app.model.cheptel;

import com.fitombo.app.model.commun.EntiteUuid;
import com.fitombo.app.model.utilisateur.Eleveur;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "propriete")
public class Propriete extends EntiteUuid {

  private String type;

  @Column(name = "part_pourcentage")
  private BigDecimal partPourcentage;

  @Column(name = "date_debut")
  private LocalDate dateDebut;

  @Column(name = "date_fin")
  private LocalDate dateFin;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "animal_id", nullable = false)
  private Animal animal;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "eleveur_id", nullable = false)
  private Eleveur eleveur;

  private String conditions;

  @Version private Long version;

  public Propriete(Animal animal, Eleveur gardien, LocalDate debut, String conditions) {
    this.animal = animal;
    this.eleveur = gardien;
    this.type = "GARDIEN";
    this.partPourcentage = new BigDecimal("100");
    this.dateDebut = debut;
    this.conditions = conditions;
  }

  public void terminer(LocalDate date) {
    dateFin = date;
  }
}
