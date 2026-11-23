package com.fitombo.app.dto.inscription;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DemandeCode(@NotBlank @Email @Size(max = 255) String email) {}
