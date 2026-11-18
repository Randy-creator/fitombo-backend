package com.fitombo.app.model.alerte;

import java.time.LocalDate;

import com.fitombo.app.model.cheptel.Animal;
import com.fitombo.app.model.cheptel.Lot;
import com.fitombo.app.model.commun.EntiteUuid;
import com.fitombo.app.model.evenement.Evenement;
import com.fitombo.app.model.exploitation.Exploitation;
import com.fitombo.app.model.objectif.Objectif;
import com.fitombo.app.model.utilisateur.Utilisateur;

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
@Table(name = "alerte")
public class Alerte extends EntiteUuid {

    private String type;

    @Column(name = "date_echeance")
    private LocalDate dateEcheance;

    private String statut;

    private String message;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exploitation_id")
    private Exploitation exploitation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "animal_id")
    private Animal animal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lot_id")
    private Lot lot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evenement_id")
    private Evenement evenement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "objectif_id")
    private Objectif objectif;
}
