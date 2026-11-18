package com.fitombo.app.model.utilisateur;

import java.time.Instant;
import java.time.LocalDate;

import com.fitombo.app.model.commun.EntiteUuid;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "eleveur")
public class Eleveur extends EntiteUuid {

    @Column(name = "deleted_at")
    private Instant deletedAt;

    private String commune;

    private String fokontany;

    @Column(name = "date_inscription")
    private LocalDate dateInscription;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organisation_id")
    private Organisation organisation;

    @Version
    private Long version;
}
