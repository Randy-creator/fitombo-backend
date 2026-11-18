package com.fitombo.app.model.utilisateur;

import com.fitombo.app.model.commun.EntiteUuid;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "utilisateur")
public class Utilisateur extends EntiteUuid {

    private String nom;

    private String telephone;

    private String email;

    private String langue;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Version
    private Long version;
}
