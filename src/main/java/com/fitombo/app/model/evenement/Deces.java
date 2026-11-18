package com.fitombo.app.model.evenement;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "deces")
@DiscriminatorValue("DECES")
@PrimaryKeyJoinColumn(name = "evenement_id")
public class Deces extends Evenement {

    private String cause;

    private Integer nombre;
}
