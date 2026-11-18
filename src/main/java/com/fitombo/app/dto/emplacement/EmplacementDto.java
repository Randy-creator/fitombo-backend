package com.fitombo.app.dto.emplacement;

import java.util.UUID;

public record EmplacementDto(UUID id, String nom, String type, Integer capacite) {}
