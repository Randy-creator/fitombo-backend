package com.fitombo.app.model.evenement;

import com.fitombo.app.model.cheptel.Animal;
import com.fitombo.app.model.cheptel.Lot;
import com.fitombo.app.model.exploitation.Exploitation;
import com.fitombo.app.model.utilisateur.Utilisateur;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "alimentation")
@DiscriminatorValue("ALIMENTATION")
@PrimaryKeyJoinColumn(name = "evenement_id")
public class Alimentation extends Evenement {

  private String aliment;

  @Column(name = "quantite_kg")
  private BigDecimal quantiteKg;

  @Column(name = "cout_ariary")
  private BigDecimal coutAriary;

  public Alimentation(
      Exploitation exploitation,
      Animal animal,
      Lot lot,
      LocalDate dateEvenement,
      Utilisateur saisiPar,
      String notes,
      String aliment,
      BigDecimal quantiteKg,
      BigDecimal coutAriary) {
    super(exploitation, animal, lot, dateEvenement, saisiPar, notes);
    this.aliment = aliment.strip();
    this.quantiteKg = quantiteKg;
    this.coutAriary = coutAriary;
  }
}
