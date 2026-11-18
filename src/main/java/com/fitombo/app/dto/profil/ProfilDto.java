package com.fitombo.app.dto.profil;

import com.fitombo.app.model.utilisateur.Role;
import java.util.UUID;

public record ProfilDto(
    UUID id, String nom, String telephone, String email, String langue, Role role) {}
