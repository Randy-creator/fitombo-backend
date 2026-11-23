package com.fitombo.app.repository.connexion;

import com.fitombo.app.model.utilisateur.Role;
import com.fitombo.app.model.utilisateur.Utilisateur;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, UUID> {

  @Query(
      """
      select u.id as id, u.role as role, u.motDePasseHash as motDePasseHash
      from Utilisateur u
      where u.actif = true
        and ((u.telephone = :telephone and u.telephoneVerifie = true)
          or (lower(u.email) = :email and u.emailVerifie = true))
        and u.deletedAt is null and u.motDePasseHash is not null
      """)
  Optional<Identifiants> findIdentifiants(
      @Param("telephone") String telephone, @Param("email") String email);

  interface Identifiants {
    UUID getId();

    Role getRole();

    String getMotDePasseHash();
  }
}
