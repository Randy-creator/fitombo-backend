package com.fitombo.app.model.evenement;

import com.fitombo.app.model.cheptel.Animal;
import com.fitombo.app.model.cheptel.Lot;
import com.fitombo.app.model.exploitation.Exploitation;
import com.fitombo.app.model.utilisateur.Utilisateur;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "mise_bas")
@DiscriminatorValue("MISE_BAS")
@PrimaryKeyJoinColumn(name = "evenement_id")
public class MiseBas extends Evenement {

  @Column(name = "nes_vivants")
  private Integer nesVivants;

  @Column(name = "morts_nes")
  private Integer mortsNes;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "saillie_id")
  private Saillie saillie;

  @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
  @JoinColumn(name = "lot_petits_id")
  private Lot lotPetits;

  private boolean assistance;

  @OneToMany(mappedBy = "miseBas", cascade = CascadeType.PERSIST)
  private List<Animal> petits = new ArrayList<>();

  public void ajouterPetit(Animal petit) {
    petit.rattacherMiseBas(this);
    petits.add(petit);
  }

  public void enregistrerLotPetits(Lot lot) {
    lotPetits = lot;
  }

  public MiseBas(
      Exploitation exploitation,
      Animal animal,
      Lot lot,
      LocalDate date,
      Utilisateur auteur,
      String notes,
      int nesVivants,
      int mortsNes,
      boolean assistance,
      Saillie saillie) {
    super(exploitation, animal, lot, date, auteur, notes);
    this.nesVivants = nesVivants;
    this.mortsNes = mortsNes;
    this.assistance = assistance;
    this.saillie = saillie;
  }
}
