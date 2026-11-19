package com.fitombo.app.model.evenement;

import com.fitombo.app.model.cheptel.Animal;
import com.fitombo.app.model.cheptel.Lot;
import com.fitombo.app.model.commun.EntiteUuid;
import com.fitombo.app.model.exploitation.Exploitation;
import com.fitombo.app.model.utilisateur.Utilisateur;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "evenement")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "type", length = 25)
public abstract class Evenement extends EntiteUuid {

  @Column(name = "date_evenement")
  private LocalDate dateEvenement;

  private String notes;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "exploitation_id", nullable = false)
  private Exploitation exploitation;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "animal_id")
  private Animal animal;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "lot_id")
  private Lot lot;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "saisi_par", nullable = false)
  private Utilisateur saisiPar;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "corrige_evenement_id")
  private Evenement corrigeEvenement;

  @Version private Long version;

  protected Evenement(
      Exploitation exploitation,
      Animal animal,
      Lot lot,
      LocalDate dateEvenement,
      Utilisateur saisiPar,
      String notes) {
    this.exploitation = exploitation;
    this.animal = animal;
    this.lot = lot;
    this.dateEvenement = dateEvenement;
    this.saisiPar = saisiPar;
    this.notes = notes;
  }

  @Enumerated(EnumType.STRING)
  @Column(name = "type", insertable = false, updatable = false)
  private TypeEvenement typeEvenement;

  @Column(insertable = false)
  private String statut = "VALIDE";

  @Column(name = "motif_annulation")
  private String motifAnnulation;

  public boolean modifieCheptel() {
    return switch (typeEvenement) {
      case null -> false;
      case DECES, MOUVEMENT, SAILLIE, MISE_BAS -> true;
      default -> false;
    };
  }

  public boolean estValide() {
    return "VALIDE".equals(statut);
  }

  public void remplacer(Evenement original) {
    corrigeEvenement = original;
  }

  public void annuler(String motif) {
    if (estValide()) {
      statut = "ANNULE";
      motifAnnulation = motif;
    }
  }

  @Column(name = "deleted_at")
  private Instant deletedAt;
}
