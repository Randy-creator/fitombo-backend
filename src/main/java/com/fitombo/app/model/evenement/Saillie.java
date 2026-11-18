package com.fitombo.app.model.evenement;

import java.time.LocalDate;

import com.fitombo.app.model.cheptel.Animal;

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
@Table(name = "saillie")
@DiscriminatorValue("SAILLIE")
@PrimaryKeyJoinColumn(name = "evenement_id")
public class Saillie extends Evenement {

    private String methode;

    @Column(name = "mise_bas_prevue")
    private LocalDate miseBasPrevue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "male_id")
    private Animal male;
}
