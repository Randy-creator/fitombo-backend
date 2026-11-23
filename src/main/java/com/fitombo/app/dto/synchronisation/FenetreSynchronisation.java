package com.fitombo.app.dto.synchronisation;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

public record FenetreSynchronisation(
    @Pattern(regexp = "[0-9]{1,18}") String transaction, @PositiveOrZero Long sequence) {
  public String transactionEffective() {
    return transaction == null ? "0" : transaction;
  }

  public long sequenceEffective() {
    return sequence == null ? 0 : sequence;
  }
}
