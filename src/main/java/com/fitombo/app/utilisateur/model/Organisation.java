package com.fitombo.app.utilisateur.model;

import com.fitombo.app.commun.model.EntiteUuid;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "organisation")
public class Organisation extends EntiteUuid {

    private String nom;

    private String type;

    @Version
    private Long version;
}
