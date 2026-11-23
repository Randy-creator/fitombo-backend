package com.fitombo.app.model.evenement;

import com.fitombo.app.model.cheptel.Animal;
import com.fitombo.app.model.cheptel.Lot;
import com.fitombo.app.model.exploitation.Exploitation;
import com.fitombo.app.model.referentiel.EtapeProtocole;
import com.fitombo.app.model.utilisateur.Utilisateur;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
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
@Table(name = "vaccination")
@DiscriminatorValue("VACCINATION")
@PrimaryKeyJoinColumn(name = "evenement_id")
public class Vaccination extends Evenement {

  private String vaccin;

  @Column(name = "rappel_prevu")
  private LocalDate rappelPrevu;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "etape_protocole_id")
  private EtapeProtocole etapeProtocole;

  @Column(name = "maladie_ciblee")
  private String maladieCiblee;

  @Column(name = "lot_vaccin")
  private String lotVaccin;

  public Vaccination(
      Exploitation exploitation,
      Animal animal,
      Lot lot,
      LocalDate dateEvenement,
      Utilisateur saisiPar,
      String notes,
      String vaccin,
      String maladieCiblee,
      String lotVaccin,
      LocalDate rappelPrevu) {
    super(exploitation, animal, lot, dateEvenement, saisiPar, notes);
    this.vaccin = vaccin.strip();
    this.maladieCiblee = maladieCiblee;
    this.lotVaccin = lotVaccin;
    this.rappelPrevu = rappelPrevu;
  }
}
