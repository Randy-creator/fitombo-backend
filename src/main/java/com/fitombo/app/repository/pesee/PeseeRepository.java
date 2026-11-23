package com.fitombo.app.repository.pesee;

import com.fitombo.app.model.evenement.Pesee;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PeseeRepository extends JpaRepository<Pesee, UUID> {}
