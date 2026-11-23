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
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "traitement")
@DiscriminatorValue("TRAITEMENT")
@PrimaryKeyJoinColumn(name = "evenement_id")
public class Traitement extends Evenement {

  private String produit;

  @Column(name = "delai_attente_jours")
  private Integer delaiAttenteJours;

  private String maladie;
  private String dose;

  @Column(name = "duree_jours")
  private Integer dureeJours;

  public Traitement(
      Exploitation exploitation,
      Animal animal,
      Lot lot,
      LocalDate dateEvenement,
      Utilisateur saisiPar,
      String notes,
      String produit,
      String maladie,
      String dose,
      Integer dureeJours,
      int delaiAttenteJours) {
    super(exploitation, animal, lot, dateEvenement, saisiPar, notes);
    this.produit = produit.strip();
    this.maladie = maladie;
    this.dose = dose;
    this.dureeJours = dureeJours;
    this.delaiAttenteJours = delaiAttenteJours;
  }
}
