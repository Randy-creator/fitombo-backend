package com.fitombo.app.model.evenement;

import com.fitombo.app.model.cheptel.Animal;
import com.fitombo.app.model.cheptel.Lot;
import com.fitombo.app.model.exploitation.Exploitation;
import com.fitombo.app.model.utilisateur.Utilisateur;
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
@Table(name = "diagnostic_gestation")
@DiscriminatorValue("DIAGNOSTIC_GESTATION")
@PrimaryKeyJoinColumn(name = "evenement_id")
public class DiagnosticGestation extends Evenement {

  private Boolean positif;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "saillie_id")
  private Saillie saillie;

  @Enumerated(EnumType.STRING)
  private MethodeDiagnostic methode;

  public DiagnosticGestation(
      Exploitation exploitation,
      Animal animal,
      Lot lot,
      LocalDate date,
      Utilisateur auteur,
      String notes,
      boolean positif,
      MethodeDiagnostic methode,
      Saillie saillie) {
    super(exploitation, animal, lot, date, auteur, notes);
    this.positif = positif;
    this.methode = methode;
    this.saillie = saillie;
  }
}
