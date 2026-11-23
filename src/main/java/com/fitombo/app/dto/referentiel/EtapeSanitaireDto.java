package com.fitombo.app.dto.referentiel;

import java.util.UUID;

public record EtapeSanitaireDto(
    UUID protocoleId,
    String protocole,
    String source,
    boolean valide,
    UUID id,
    int ordre,
    int ageJours,
    String typeActe,
    String produit,
    Integer rappelApresJours) {}
