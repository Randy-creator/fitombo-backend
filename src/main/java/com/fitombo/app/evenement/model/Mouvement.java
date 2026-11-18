package com.fitombo.app.evenement.model;

import com.fitombo.app.exploitation.model.Emplacement;
import com.fitombo.app.exploitation.model.Exploitation;
import com.fitombo.app.exploitation.model.Tiers;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tiers_id")
    private Tiers tiers;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emplacement_destination_id")
    private Emplacement emplacementDestination;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exploitation_destination_id")
    private Exploitation exploitationDestination;
}
