package com.fitombo.app.evenement.model;

import java.time.LocalDate;

import com.fitombo.app.referentiel.model.EtapeProtocole;

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
@Table(name = "vaccination")
@DiscriminatorValue("VACCINATION")
@PrimaryKeyJoinColumn(name = "evenement_id")
public class Vaccination extends Evenement {

    private String vaccin;

    @Column(name = "rappel_prevu")
    private LocalDate rappelPrevu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "etape_protocole_id")
    private EtapeProtocole etapeProtocole;
}
