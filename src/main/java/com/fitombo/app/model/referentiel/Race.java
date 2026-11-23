package com.fitombo.app.model.referentiel;

import com.fitombo.app.model.commun.EntiteUuid;
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
@Table(name = "race")
public class Race extends EntiteUuid {

  private boolean actif;

  private String nom;

  private String origine;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "espece_code", nullable = false)
  private Espece espece;
}
