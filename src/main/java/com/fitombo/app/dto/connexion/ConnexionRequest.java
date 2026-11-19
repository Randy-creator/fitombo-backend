package com.fitombo.app.dto.connexion;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ConnexionRequest(
    @Pattern(regexp = "\\+[1-9][0-9]{7,14}") String telephone,
    @Email @Size(min = 1, max = 255) String email,
    @NotEmpty @Size(max = 72) String motDePasse) {
  @AssertTrue(message = "Choisir un e-mail ou un téléphone")
  public boolean isIdentifiantUnique() {
    return (email == null) != (telephone == null);
  }

  @Override
  public String toString() {
    return "ConnexionRequest";
  }
}
