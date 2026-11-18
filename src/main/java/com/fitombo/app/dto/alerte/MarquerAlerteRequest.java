package com.fitombo.app.dto.alerte;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record MarquerAlerteRequest(@NotNull @Pattern(regexp = "VUE") String statut) {}
