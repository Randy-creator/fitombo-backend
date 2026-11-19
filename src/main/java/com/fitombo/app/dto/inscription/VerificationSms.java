package com.fitombo.app.dto.inscription;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record VerificationSms(
    @NotNull UUID demandeId,
    @NotNull @Pattern(regexp = "\\+[1-9][0-9]{7,14}") String telephone,
    @NotNull @Pattern(regexp = "[0-9]{6}") String code,
    @NotBlank @Size(max = 150) String nom,
    @Email @Size(max = 255) String email,
    @NotNull @Pattern(regexp = "fr|mg") String langue,
    @NotBlank @Size(min = 8, max = 72) String motDePasse) {
  @Override
  public String toString() {
    return "VerificationSms";
  }
}
