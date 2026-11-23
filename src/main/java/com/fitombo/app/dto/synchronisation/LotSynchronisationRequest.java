package com.fitombo.app.dto.synchronisation;

import com.fitombo.app.dto.evenement.SaisieEvenement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public record LotSynchronisationRequest(
    @NotEmpty @Size(max = 50) List<@NotNull @Valid Operation> operations) {
  public record Operation(@NotNull UUID operationId, @NotNull SaisieEvenement saisie) {}
}
