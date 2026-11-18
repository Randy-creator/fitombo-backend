package com.fitombo.app.model.evenement;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "production")
@DiscriminatorValue("PRODUCTION")
@PrimaryKeyJoinColumn(name = "evenement_id")
public class Production extends Evenement {

  @Column(name = "type_production")
  private String typeProduction;

  private BigDecimal quantite;

  private String unite;
}
