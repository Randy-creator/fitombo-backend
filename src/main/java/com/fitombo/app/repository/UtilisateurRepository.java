package com.fitombo.app.repository;

import java.util.Optional;
import java.util.UUID;

import com.fitombo.app.model.utilisateur.Role;
import com.fitombo.app.model.utilisateur.Utilisateur;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, UUID> {

    @Query("""
            select u.id as id, u.role as role, u.motDePasseHash as motDePasseHash
            from Utilisateur u
            where u.telephone = :telephone and u.actif = true and u.telephoneVerifie = true
              and u.deletedAt is null and u.motDePasseHash is not null
            """)
    Optional<Identifiants> findIdentifiants(@Param("telephone") String telephone);

    interface Identifiants {
        UUID getId();
        Role getRole();
        String getMotDePasseHash();
    }
}
