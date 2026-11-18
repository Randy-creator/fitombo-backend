package com.fitombo.app.model.referentiel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "espece")
public class Espece {

    private boolean actif;

    @Id
    private String code;

    @Column(name = "nom_fr")
    private String nomFr;

    @Column(name = "nom_mg")
    private String nomMg;

    @Column(name = "mode_suivi")
    @Enumerated(EnumType.STRING)
    private ModeSuivi modeSuivi;
}
