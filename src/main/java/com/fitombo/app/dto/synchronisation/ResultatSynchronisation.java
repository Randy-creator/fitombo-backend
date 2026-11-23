package com.fitombo.app.dto.synchronisation;

import com.fitombo.app.exception.CodeErreur;
import java.util.UUID;

public record ResultatSynchronisation(
    UUID operationId, Statut statut, UUID evenementId, CodeErreur code) {
  public enum Statut {
    ACCEPTE,
    DOUBLON,
    REJETE
  }
}
