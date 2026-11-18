package com.fitombo.app.dto;

import com.fitombo.app.model.referentiel.ModeSuivi;

public record EspeceDto(String code, String nomFr, String nomMg, ModeSuivi modeSuivi) {}
