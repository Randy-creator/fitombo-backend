package com.fitombo.app.model.utilisateur;

import com.fitombo.app.model.commun.EntiteUuid;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "utilisateur")
public class Utilisateur extends EntiteUuid {

  private boolean actif;

  @Column(name = "deleted_at")
  private Instant deletedAt;

  private String nom;

  private String telephone;

  @Column(name = "telephone_verifie")
  private boolean telephoneVerifie;

  @Getter(AccessLevel.NONE)
  @Column(name = "mot_de_passe_hash")
  private String motDePasseHash;

  private String email;

  private String langue;

  @Enumerated(EnumType.STRING)
  private Role role;

  @Version private Long version;

  public void modifierProfil(String nom, String langue) {
    this.nom = nom.strip();
    this.langue = langue;
  }
}
