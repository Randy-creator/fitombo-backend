package com.fitombo.app.referentiel.model;

import java.math.BigDecimal;

import com.fitombo.app.commun.model.EntiteUuid;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "point_courbe")
public class PointCourbe extends EntiteUuid {

    @Column(name = "age_jours")
    private Integer ageJours;

    @Column(name = "poids_attendu_kg")
    private BigDecimal poidsAttenduKg;

    @Column(name = "poids_min_kg")
    private BigDecimal poidsMinKg;

    @Column(name = "poids_max_kg")
    private BigDecimal poidsMaxKg;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "courbe_id", nullable = false)
    private CourbeReference courbe;
}
