package com.fitombo.app.evenement.model;

import java.math.BigDecimal;

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
@Table(name = "alimentation")
@DiscriminatorValue("ALIMENTATION")
@PrimaryKeyJoinColumn(name = "evenement_id")
public class Alimentation extends Evenement {

    private String aliment;

    @Column(name = "quantite_kg")
    private BigDecimal quantiteKg;
}
