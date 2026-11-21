package com.fitombo.app.dto.membre;

import com.fitombo.app.model.exploitation.RoleExploitation;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record InvitationMembreRequest(
    @NotNull UUID utilisateurId, @NotNull RoleExploitation role) {}
