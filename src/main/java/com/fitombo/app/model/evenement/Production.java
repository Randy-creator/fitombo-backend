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
@Table(name = "production")
@DiscriminatorValue("PRODUCTION")
@PrimaryKeyJoinColumn(name = "evenement_id")
public class Production extends Evenement {

  @Column(name = "type_production")
  private String typeProduction;

  private BigDecimal quantite;

  private String unite;

  public Production(
      Exploitation exploitation,
      Animal animal,
      Lot lot,
      LocalDate dateEvenement,
      Utilisateur saisiPar,
      String notes,
      String typeProduction,
      BigDecimal quantite,
      String unite) {
    super(exploitation, animal, lot, dateEvenement, saisiPar, notes);
    this.typeProduction = typeProduction;
    this.quantite = quantite;
    this.unite = unite;
  }
}
