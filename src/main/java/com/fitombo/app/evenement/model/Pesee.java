package com.fitombo.app.evenement.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

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
}
