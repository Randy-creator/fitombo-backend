package com.fitombo.app.repository.mouvement;

import com.fitombo.app.model.cheptel.Propriete;
import com.fitombo.app.model.utilisateur.Eleveur;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

public interface ConfiageRepository extends Repository<Propriete, UUID> {
  Propriete save(Propriete confiage);

  Optional<Propriete> findByAnimalIdAndTypeAndDateFinIsNull(UUID animal, String type);

  @Query(
      "select e from Eleveur e where e.id = :id and e.deletedAt is null and e.utilisateur.actif = true and e.utilisateur.deletedAt is null")
  Optional<Eleveur> trouverGardien(UUID id);
}
