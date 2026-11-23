package com.fitombo.app.dto.inscription;

import java.util.UUID;

public record DemandeVerificationDto(UUID demandeId, long expiresIn) {}
