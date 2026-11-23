package com.fitombo.app.model.evenement;

import com.fitombo.app.model.cheptel.Animal;
import com.fitombo.app.model.cheptel.Lot;
import com.fitombo.app.model.exploitation.Exploitation;
import com.fitombo.app.model.utilisateur.Utilisateur;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "saillie")
@DiscriminatorValue("SAILLIE")
@PrimaryKeyJoinColumn(name = "evenement_id")
public class Saillie extends Evenement {

  @Enumerated(EnumType.STRING)
  private MethodeSaillie methode;

  @Column(name = "mise_bas_prevue")
  private LocalDate miseBasPrevue;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "male_id")
  private Animal male;

  @Column(name = "male_externe")
  private String maleExterne;

  @Column(name = "reference_semence")
  private String referenceSemence;

  public Saillie(
      Exploitation exploitation,
      Animal animal,
      Lot lot,
      LocalDate date,
      Utilisateur auteur,
      String notes,
      MethodeSaillie methode,
      Animal male,
      String maleExterne,
      String referenceSemence,
      LocalDate miseBasPrevue) {
    super(exploitation, animal, lot, date, auteur, notes);
    this.methode = methode;
    this.male = male;
    this.maleExterne = maleExterne;
    this.referenceSemence = referenceSemence;
    this.miseBasPrevue = miseBasPrevue;
  }
}
