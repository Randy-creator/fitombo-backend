package com.fitombo.app.dto.connexion;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ConnexionRequest(
    @NotNull @Pattern(regexp = "\\+[1-9][0-9]{7,14}") String telephone,
    @NotEmpty @Size(max = 72) String motDePasse) {
  @Override
  public String toString() {
    return "ConnexionRequest";
  }
}
