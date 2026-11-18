package com.fitombo.app.evenement.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "diagnostic_gestation")
@DiscriminatorValue("DIAGNOSTIC_GESTATION")
@PrimaryKeyJoinColumn(name = "evenement_id")
public class DiagnosticGestation extends Evenement {

    private Boolean positif;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "saillie_id")
    private Saillie saillie;
}
