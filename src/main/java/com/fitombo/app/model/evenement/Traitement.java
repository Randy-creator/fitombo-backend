package com.fitombo.app.model.evenement;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
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
}
