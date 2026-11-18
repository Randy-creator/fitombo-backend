package com.fitombo.app.evenement.model;

import com.fitombo.app.cheptel.model.Lot;

import jakarta.persistence.Column;
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
@Table(name = "mise_bas")
@DiscriminatorValue("MISE_BAS")
@PrimaryKeyJoinColumn(name = "evenement_id")
public class MiseBas extends Evenement {

    @Column(name = "nes_vivants")
    private Integer nesVivants;

    @Column(name = "morts_nes")
    private Integer mortsNes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "saillie_id")
    private Saillie saillie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lot_petits_id")
    private Lot lotPetits;
}
