package com.fitombo.app.model.evenement;

import com.fitombo.app.model.cheptel.Animal;
import com.fitombo.app.model.cheptel.Lot;
import com.fitombo.app.model.exploitation.Emplacement;
import com.fitombo.app.model.exploitation.Exploitation;
import com.fitombo.app.model.exploitation.Tiers;
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
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "mouvement")
@DiscriminatorValue("MOUVEMENT")
@PrimaryKeyJoinColumn(name = "evenement_id")
public class Mouvement extends Evenement {

  @Column(name = "type_mouvement")
  @Enumerated(EnumType.STRING)
  private TypeMouvement typeMouvement;

  private Integer quantite;

  @ManyToOne(fetch = FetchType.LAZY, cascade = jakarta.persistence.CascadeType.PERSIST)
  @JoinColumn(name = "tiers_id")
  private Tiers tiers;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "emplacement_destination_id")
  private Emplacement emplacementDestination;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "exploitation_destination_id")
  private Exploitation exploitationDestination;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "exploitation_origine_id")
  private Exploitation exploitationOrigine;

  @Column(name = "prix_total_ariary")
  private BigDecimal prixTotalAriary;

  @Column(name = "mode_prix")
  private String modePrix;

  @Column(name = "poids_total_kg")
  private BigDecimal poidsTotalKg;

  @Column(name = "numero_piece_circulation")
  private String numeroPieceCirculation;

  public Mouvement(
      Exploitation exploitation,
      Animal animal,
      Lot lot,
      LocalDate date,
      Utilisateur auteur,
      String notes,
      TypeMouvement typeMouvement,
      int quantite,
      BigDecimal prixTotalAriary,
      String modePrix,
      BigDecimal poidsTotalKg,
      String numeroPieceCirculation,
      Tiers tiers,
      Exploitation destination,
      Emplacement emplacement) {
    super(exploitation, animal, lot, date, auteur, notes);
    this.typeMouvement = typeMouvement;
    this.quantite = quantite;
    this.prixTotalAriary = prixTotalAriary;
    this.modePrix = modePrix;
    this.poidsTotalKg = poidsTotalKg;
    this.numeroPieceCirculation = numeroPieceCirculation;
    this.tiers = tiers;
    this.exploitationDestination = destination;
    this.exploitationOrigine = exploitation;
    this.emplacementDestination = emplacement;
  }
}
