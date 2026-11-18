package com.fitombo.app.repository.profil;

import com.fitombo.app.model.utilisateur.Utilisateur;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.Repository;

public interface ProfilRepository extends Repository<Utilisateur, UUID> {
  Optional<Utilisateur> findByIdAndActifTrueAndDeletedAtIsNull(UUID id);
}
