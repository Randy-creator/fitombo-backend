package com.fitombo.app.finance.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fitombo.app.cheptel.model.Animal;
import com.fitombo.app.cheptel.model.Lot;
import com.fitombo.app.commun.model.EntiteUuid;
import com.fitombo.app.evenement.model.Evenement;
import com.fitombo.app.exploitation.model.Exploitation;
import com.fitombo.app.exploitation.model.Tiers;
import com.fitombo.app.utilisateur.model.Utilisateur;

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
@Table(name = "transaction_financiere")
public class TransactionFinanciere extends EntiteUuid {

    private String type;

    private String categorie;

    @Column(name = "montant_ariary")
    private BigDecimal montantAriary;

    @Column(name = "date_transaction")
    private LocalDate dateTransaction;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exploitation_id", nullable = false)
    private Exploitation exploitation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evenement_id")
    private Evenement evenement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tiers_id")
    private Tiers tiers;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "animal_id")
    private Animal animal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lot_id")
    private Lot lot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "saisi_par")
    private Utilisateur saisiPar;

    @Version
    private Long version;
}
