package com.fitombo.app.model.referentiel;

import com.fitombo.app.model.commun.EntiteUuid;
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
@Table(name = "etape_protocole")
public class EtapeProtocole extends EntiteUuid {

  @Column(name = "age_jours")
  private Integer ageJours;

  private String produit;

  private String description;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "protocole_id", nullable = false)
  private ProtocoleSanitaire protocole;
}
