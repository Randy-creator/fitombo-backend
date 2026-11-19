package com.fitombo.app.model.cheptel;

import com.fitombo.app.model.commun.EntiteUuid;
import com.fitombo.app.model.evenement.MiseBas;
import com.fitombo.app.model.exploitation.Emplacement;
import com.fitombo.app.model.exploitation.Exploitation;
import com.fitombo.app.model.referentiel.Espece;
import com.fitombo.app.model.referentiel.Race;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
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
@Table(name = "animal")
public class Animal extends EntiteUuid {

  @Column(name = "deleted_at")
  private Instant deletedAt;

  private String identifiant;

  private String nom;

  private String sexe;

  @Column(name = "date_naissance")
  private LocalDate dateNaissance;

  private String origine;

  @Enumerated(EnumType.STRING)
  private StatutAnimal statut;

  @Column(name = "date_sortie")
  private LocalDate dateSortie;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "exploitation_id", nullable = false)
  private Exploitation exploitation;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "espece_code", nullable = false)
  private Espece espece;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "race_id")
  private Race race;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "emplacement_id")
  private Emplacement emplacement;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "lot_id")
  private Lot lot;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "mere_id")
  private Animal mere;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "pere_id")
  private Animal pere;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "mise_bas_id")
  private MiseBas miseBas;

  @Version private Long version;

  private String boucle;

  @Column(name = "marque_traditionnelle")
  private String marqueTraditionnelle;

  @Column(name = "date_naissance_estimee")
  private boolean dateNaissanceEstimee;

  @Column(name = "photo_url")
  private String photoUrl;

  public Animal(
      Exploitation exploitation,
      Espece espece,
      String identifiant,
      SexeAnimal sexe,
      OrigineAnimal origine) {
    this.exploitation = exploitation;
    this.espece = espece;
    this.identifiant = identifiant.strip();
    this.sexe = sexe.name();
    this.origine = origine.name();
    this.statut = StatutAnimal.ACTIF;
  }

  public void renseignerIdentification(
      String nom,
      String boucle,
      String marqueTraditionnelle,
      LocalDate dateNaissance,
      boolean dateNaissanceEstimee,
      String photoUrl) {
    this.nom = nom;
    this.boucle = boucle == null || boucle.isBlank() ? null : boucle.strip();
    this.marqueTraditionnelle = marqueTraditionnelle;
    this.dateNaissance = dateNaissance;
    this.dateNaissanceEstimee = dateNaissanceEstimee;
    this.photoUrl = photoUrl;
  }

  public void renseignerReferences(Race race, Emplacement emplacement, Animal mere, Animal pere) {
    this.race = race;
    this.emplacement = emplacement;
    this.mere = mere;
    this.pere = pere;
  }

  public void enregistrerSortie(StatutAnimal statut, LocalDate date) {
    this.statut = statut;
    this.dateSortie = date;
  }

  public void rattacherMiseBas(MiseBas miseBas) {
    this.miseBas = miseBas;
  }

  public void transferer(Exploitation destination, Emplacement emplacement) {
    this.exploitation = destination;
    this.emplacement = emplacement;
    this.lot = null;
  }

  public void affecterLot(Lot nouveauLot) {
    this.lot = nouveauLot;
  }
}
