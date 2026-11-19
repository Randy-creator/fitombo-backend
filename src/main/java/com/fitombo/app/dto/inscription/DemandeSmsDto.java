package com.fitombo.app.dto.inscription;

import java.util.UUID;

public record DemandeSmsDto(UUID demandeId, long expiresIn) {}
