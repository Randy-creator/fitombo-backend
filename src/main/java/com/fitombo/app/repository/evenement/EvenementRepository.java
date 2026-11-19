package com.fitombo.app.repository.evenement;

import com.fitombo.app.model.evenement.Evenement;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvenementRepository extends JpaRepository<Evenement, UUID> {}
