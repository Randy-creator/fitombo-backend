package com.fitombo.app.repository.objectif;

import com.fitombo.app.model.objectif.Objectif;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ObjectifRepository extends JpaRepository<Objectif, UUID> {
  Page<Objectif> findByExploitationIdAndDeletedAtIsNull(UUID exploitationId, Pageable page);

  Optional<Objectif> findByIdAndDeletedAtIsNull(UUID id);
}
