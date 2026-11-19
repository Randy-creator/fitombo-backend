package com.fitombo.app.dto.inscription;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record DemandeCode(@NotNull @Pattern(regexp = "\\+[1-9][0-9]{7,14}") String telephone) {}
