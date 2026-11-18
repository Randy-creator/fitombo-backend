package com.fitombo.app.cheptel.model;

import java.time.LocalDate;

import com.fitombo.app.commun.model.EntiteUuid;
import com.fitombo.app.exploitation.model.Emplacement;
import com.fitombo.app.exploitation.model.Exploitation;
import com.fitombo.app.referentiel.model.Espece;
import com.fitombo.app.referentiel.model.Race;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "lot")
public class Lot extends EntiteUuid {

    private String code;

    @Column(name = "date_creation")
    private LocalDate dateCreation;

    @Column(name = "effectif_initial")
    private Integer effectifInitial;

    @Column(name = "effectif_actuel")
    private Integer effectifActuel;

    private String statut;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exploitation_id", nullable = false)
    private Exploitation exploitation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "espece_code", nullable = false)
    private Espece espece;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "race_id")
    private Race race;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emplacement_id")
    private Emplacement emplacement;

    @Version
    private Long version;
}
