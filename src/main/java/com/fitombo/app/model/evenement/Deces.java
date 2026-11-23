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
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "deces")
@DiscriminatorValue("DECES")
@PrimaryKeyJoinColumn(name = "evenement_id")
public class Deces extends Evenement {

  @Enumerated(EnumType.STRING)
  private CauseDeces cause;

  private Integer nombre;

  @Column(name = "maladie_suspectee")
  private String maladieSuspectee;

  @Column(name = "declaration_faite")
  private boolean declarationFaite;

  public Deces(
      Exploitation exploitation,
      Animal animal,
      Lot lot,
      LocalDate date,
      Utilisateur auteur,
      String notes,
      CauseDeces cause,
      int nombre,
      String maladieSuspectee,
      boolean declarationFaite) {
    super(exploitation, animal, lot, date, auteur, notes);
    this.cause = cause;
    this.nombre = nombre;
    this.maladieSuspectee = maladieSuspectee;
    this.declarationFaite = declarationFaite;
  }
}
