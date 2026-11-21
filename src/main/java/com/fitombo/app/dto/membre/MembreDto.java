package com.fitombo.app.dto.membre;

import com.fitombo.app.model.exploitation.RoleExploitation;
import java.time.LocalDate;
import java.util.UUID;

public record MembreDto(
    UUID id,
    UUID exploitationId,
    UUID utilisateurId,
    RoleExploitation role,
    String statut,
    LocalDate dateDebut,
    LocalDate dateFin) {}
