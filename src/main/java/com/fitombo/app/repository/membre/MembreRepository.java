package com.fitombo.app.repository.membre;

import com.fitombo.app.dto.membre.MembreDto;
import com.fitombo.app.model.exploitation.MembreExploitation;
import com.fitombo.app.model.utilisateur.Utilisateur;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface MembreRepository extends JpaRepository<MembreExploitation, UUID> {
  String PROJECTION =
      """
      select new com.fitombo.app.dto.membre.MembreDto(m.id, m.exploitation.id, m.utilisateur.id,
          m.role, m.statut, m.dateDebut, m.dateFin)
      from MembreExploitation m where
      """;

  @Query(PROJECTION + "m.exploitation.id = :exploitationId and m.statut <> 'REVOQUE'")
  Page<MembreDto> lister(UUID exploitationId, Pageable page);

  @Query(
      PROJECTION
          + """
          m.utilisateur.id = :utilisateurId and m.statut = 'INVITE' and m.exploitation.deletedAt is null
          """)
  Page<MembreDto> invitations(UUID utilisateurId, Pageable page);

  @Query("select u from Utilisateur u where u.id = :id and u.actif = true and u.deletedAt is null")
  Optional<Utilisateur> compteActif(UUID id);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  Optional<MembreExploitation> findByIdAndExploitationId(UUID id, UUID exploitationId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  Optional<MembreExploitation> findByIdAndUtilisateurId(UUID id, UUID utilisateurId);
}
