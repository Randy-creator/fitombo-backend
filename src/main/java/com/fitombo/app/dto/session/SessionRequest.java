package com.fitombo.app.dto.session;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record SessionRequest(@NotNull @Pattern(regexp = "[A-Za-z0-9_-]{43}") String refreshToken) {
  @Override
  public String toString() {
    return "SessionRequest";
  }
}
