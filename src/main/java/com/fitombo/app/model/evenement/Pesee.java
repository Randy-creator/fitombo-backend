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
@Table(name = "pesee")
@DiscriminatorValue("PESEE")
@PrimaryKeyJoinColumn(name = "evenement_id")
public class Pesee extends Evenement {

  @Column(name = "poids_kg")
  private BigDecimal poidsKg;

  @Enumerated(EnumType.STRING)
  private MethodePesee methode;

  @Column(name = "perimetre_thoracique_cm")
  private BigDecimal perimetreThoraciqueCm;

  @Column(name = "longueur_cm")
  private BigDecimal longueurCm;

  @Column(name = "nb_echantillon")
  private Integer nbEchantillon;

  public Pesee(
      Exploitation exploitation,
      Animal animal,
      Lot lot,
      LocalDate dateEvenement,
      Utilisateur saisiPar,
      String notes,
      MethodePesee methode,
      BigDecimal poidsKg) {
    super(exploitation, animal, lot, dateEvenement, saisiPar, notes);
    this.methode = methode;
    this.poidsKg = poidsKg;
  }

  public void renseignerMesures(
      BigDecimal perimetreThoraciqueCm, BigDecimal longueurCm, Integer nbEchantillon) {
    this.perimetreThoraciqueCm = perimetreThoraciqueCm;
    this.longueurCm = longueurCm;
    this.nbEchantillon = nbEchantillon;
  }
}
